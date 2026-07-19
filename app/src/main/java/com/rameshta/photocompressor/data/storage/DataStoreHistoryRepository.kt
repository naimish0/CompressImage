package com.rameshta.photocompressor.data.storage

import android.content.Context
import android.provider.MediaStore
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rameshta.photocompressor.BuildConfig
import com.rameshta.photocompressor.di.IoDispatcher
import com.rameshta.photocompressor.domain.model.CompressionMode
import com.rameshta.photocompressor.domain.model.HistoryOperationType
import com.rameshta.photocompressor.domain.model.ImageFormat
import com.rameshta.photocompressor.domain.model.ImageInfo
import com.rameshta.photocompressor.domain.model.ProcessedImage
import com.rameshta.photocompressor.domain.model.ProcessingNotice
import com.rameshta.photocompressor.domain.repository.HistoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

private val Context.historyDataStore by preferencesDataStore(name = "processed_image_history")

@Singleton
class DataStoreHistoryRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : HistoryRepository {
    override val history: Flow<List<ProcessedImage>> =
        context.historyDataStore.data
            .map { preferences ->
                decodeHistory(preferences[KEY_HISTORY].orEmpty())
                    .filter { outputExists(it.filePath) }
                    .sortedByDescending { it.createdTimestamp }
            }
            .flowOn(ioDispatcher)

    override suspend fun recordSuccessfulOutput(
        output: ProcessedImage,
        operationType: HistoryOperationType,
    ): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val entry = output.copy(
                operationType = operationType,
                createdTimestamp = if (output.createdTimestamp > 0L) {
                    output.createdTimestamp
                } else {
                    System.currentTimeMillis()
                },
            )
            context.historyDataStore.edit { preferences ->
                val current = decodeHistory(preferences[KEY_HISTORY].orEmpty())
                val updated = (listOf(entry) + current)
                    .distinctBy { historyIdentity(it) }
                    .take(MAX_HISTORY_ITEMS)
                preferences[KEY_HISTORY] = encodeHistory(updated)
            }
            Unit
        }
    }

    override suspend fun remove(id: String) {
        withContext(ioDispatcher) {
            var removed = emptyList<ProcessedImage>()
            context.historyDataStore.edit { preferences ->
                val current = decodeHistory(preferences[KEY_HISTORY].orEmpty())
                removed = current.filter { it.id == id }
                val updated = current.filterNot { it.id == id }
                preferences[KEY_HISTORY] = encodeHistory(updated)
            }
            removed.forEach(::deleteAppOwnedTempOutput)
        }
    }

    override suspend fun clear() {
        withContext(ioDispatcher) {
            var removed = emptyList<ProcessedImage>()
            context.historyDataStore.edit { preferences ->
                removed = decodeHistory(preferences[KEY_HISTORY].orEmpty())
                preferences[KEY_HISTORY] = "[]"
            }
            removed.forEach(::deleteAppOwnedTempOutput)
        }
    }

    private fun decodeHistory(payload: String): List<ProcessedImage> {
        if (payload.isBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(payload)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    decodeProcessedImage(item)?.let(::add)
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun encodeHistory(history: List<ProcessedImage>): String {
        val array = JSONArray()
        history.forEach { array.put(encodeProcessedImage(it)) }
        return array.toString()
    }

    private fun decodeProcessedImage(json: JSONObject): ProcessedImage? {
        val original = decodeImageInfo(json.optJSONObject("original") ?: return null) ?: return null
        val format = json.optEnum("format", ImageFormat.UNKNOWN)
        return ProcessedImage(
            id = json.optString("id").takeIf { it.isNotBlank() } ?: return null,
            original = original,
            filePath = json.optString("filePath").takeIf { it.isNotBlank() } ?: return null,
            displayName = json.optString("displayName"),
            sizeBytes = json.optLong("sizeBytes", 0L),
            width = json.optInt("width", 0),
            height = json.optInt("height", 0),
            format = format,
            mimeType = json.optString("mimeType").takeIf { it.isNotBlank() } ?: format.mimeType,
            requestedTargetBytes = json.optNullableLong("requestedTargetBytes"),
            targetLimitBytes = json.optNullableLong("targetLimitBytes"),
            targetReached = json.optNullableBoolean("targetReached"),
            outputQuality = json.optNullableInt("outputQuality"),
            compressionMode = json.optEnumOrNull<CompressionMode>("compressionMode"),
            operationType = json.optEnum("operationType", HistoryOperationType.COMPRESSED),
            savedUriString = json.optNullableString("savedUriString"),
            createdTimestamp = json.optLong("createdTimestamp", System.currentTimeMillis()),
            warning = decodeProcessingNotice(json.optNullableString("warning")),
        )
    }

    private fun encodeProcessedImage(image: ProcessedImage): JSONObject {
        return JSONObject()
            .put("id", image.id)
            .put("original", encodeImageInfo(image.original))
            .put("filePath", image.filePath)
            .put("displayName", image.displayName)
            .put("sizeBytes", image.sizeBytes)
            .put("width", image.width)
            .put("height", image.height)
            .put("format", image.format.name)
            .put("mimeType", image.mimeType)
            .putNullable("requestedTargetBytes", image.requestedTargetBytes)
            .putNullable("targetLimitBytes", image.targetLimitBytes)
            .putNullable("targetReached", image.targetReached)
            .putNullable("outputQuality", image.outputQuality)
            .putNullable("compressionMode", image.compressionMode?.name)
            .put("operationType", image.operationType.name)
            .putNullable("savedUriString", image.savedUriString)
            .put("createdTimestamp", image.createdTimestamp)
            .putNullable("warning", image.warning?.name)
    }

    private fun decodeImageInfo(json: JSONObject): ImageInfo? {
        val format = json.optEnum("format", ImageFormat.UNKNOWN)
        return ImageInfo(
            id = json.optString("id").takeIf { it.isNotBlank() } ?: return null,
            uriString = json.optString("uriString").takeIf { it.isNotBlank() } ?: return null,
            displayName = json.optString("displayName"),
            sizeBytes = json.optLong("sizeBytes", 0L),
            width = json.optInt("width", 0),
            height = json.optInt("height", 0),
            format = format,
            mimeType = json.optString("mimeType").takeIf { it.isNotBlank() } ?: format.mimeType,
            hasAlpha = json.optBoolean("hasAlpha", format.supportsTransparency),
        )
    }

    private fun encodeImageInfo(image: ImageInfo): JSONObject {
        return JSONObject()
            .put("id", image.id)
            .put("uriString", image.uriString)
            .put("displayName", image.displayName)
            .put("sizeBytes", image.sizeBytes)
            .put("width", image.width)
            .put("height", image.height)
            .put("format", image.format.name)
            .put("mimeType", image.mimeType)
            .put("hasAlpha", image.hasAlpha)
    }

    private fun decodeProcessingNotice(value: String?): ProcessingNotice? {
        if (value == null) return null
        return enumValues<ProcessingNotice>().firstOrNull { it.name == value }
            ?: LEGACY_NOTICE_VALUES[value]
    }

    private fun outputExists(path: String): Boolean {
        return runCatching {
            val uri = path.toUri()
            if (uri.scheme == "content") {
                if (!isAllowedStoredContentUri(uri)) return false
                context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { descriptor ->
                    descriptor.length != 0L
                } ?: false
            } else {
                File(path).let { it.isFile && it.length() > 0L }
            }
        }.getOrDefault(false)
    }

    private fun historyIdentity(image: ProcessedImage): String {
        return image.id
    }

    private fun deleteAppOwnedTempOutput(image: ProcessedImage) {
        runCatching {
            val uri = image.filePath.toUri()
            if (uri.scheme != null && uri.scheme != "file") return
            val path = if (uri.scheme == "file") uri.path.orEmpty() else image.filePath
            val file = File(path).canonicalFile
            if (!file.isFile) return
            if (appOwnedTempOutputDirs().any { directory -> file.isInside(directory) }) {
                file.delete()
            }
        }
    }

    private fun appOwnedTempOutputDirs(): List<File> {
        return listOf(
            File(context.cacheDir, "processed"),
            File(context.cacheDir, "background_removal"),
        ).map { it.canonicalFile }
    }

    private fun File.isInside(directory: File): Boolean {
        return path == directory.path || path.startsWith(directory.path + File.separator)
    }

    private fun isAllowedStoredContentUri(uri: android.net.Uri): Boolean {
        val authority = uri.authority.orEmpty()
        return authority == MediaStore.AUTHORITY || authority == FILE_PROVIDER_AUTHORITY
    }

    private companion object {
        val KEY_HISTORY = stringPreferencesKey("history_json")
        val FILE_PROVIDER_AUTHORITY = "${BuildConfig.APPLICATION_ID}.fileprovider"
        const val MAX_HISTORY_ITEMS = 200
        val LEGACY_NOTICE_VALUES = mapOf(
            "PNG is lossless and the original resolution was preserved." to
                ProcessingNotice.PNG_LOSSLESS_ORIGINAL_PRESERVED,
            "PNG is lossless, so size reduction was performed through safe resizing." to
                ProcessingNotice.PNG_LOSSLESS_SAFE_RESIZE,
            "PNG is lossless. The original resolution was preserved, so the exact target could not be reached." to
                ProcessingNotice.PNG_TARGET_UNREACHED_ORIGINAL,
            "PNG is lossless. Best quality result created, but the exact target could not be reached without significant resolution loss." to
                ProcessingNotice.PNG_TARGET_UNREACHED_BEST_QUALITY,
            "Original resolution preserved. The exact target could not be reached at an acceptable encoder quality." to
                ProcessingNotice.TARGET_UNREACHED_ORIGINAL,
            "Best quality result created. The exact target could not be reached without significant quality loss." to
                ProcessingNotice.TARGET_UNREACHED_BEST_QUALITY,
            "Target reached by reducing resolution while preserving acceptable encoder quality." to
                ProcessingNotice.TARGET_REACHED_REDUCED_RESOLUTION,
            "Processed at a safe resolution to avoid running out of memory on this device." to
                ProcessingNotice.BACKGROUND_SAFE_RESOLUTION,
        )
    }
}

private fun JSONObject.putNullable(name: String, value: Any?): JSONObject {
    return put(name, value ?: JSONObject.NULL)
}

private fun JSONObject.optNullableString(name: String): String? {
    return if (isNull(name)) null else optString(name).takeIf { it.isNotBlank() }
}

private fun JSONObject.optNullableLong(name: String): Long? {
    return if (isNull(name) || !has(name)) null else optLong(name)
}

private fun JSONObject.optNullableInt(name: String): Int? {
    return if (isNull(name) || !has(name)) null else optInt(name)
}

private fun JSONObject.optNullableBoolean(name: String): Boolean? {
    return if (isNull(name) || !has(name)) null else optBoolean(name)
}

private inline fun <reified T : Enum<T>> JSONObject.optEnum(name: String, fallback: T): T {
    return optEnumOrNull<T>(name) ?: fallback
}

private inline fun <reified T : Enum<T>> JSONObject.optEnumOrNull(name: String): T? {
    val value = optNullableString(name) ?: return null
    return enumValues<T>().firstOrNull { it.name == value }
}
