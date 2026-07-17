package com.example.compressimage.data.storage

import android.content.Context
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.compressimage.di.IoDispatcher
import com.example.compressimage.domain.model.CompressionMode
import com.example.compressimage.domain.model.HistoryOperationType
import com.example.compressimage.domain.model.ImageFormat
import com.example.compressimage.domain.model.ImageInfo
import com.example.compressimage.domain.model.ProcessedImage
import com.example.compressimage.domain.repository.HistoryRepository
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
            context.historyDataStore.edit { preferences ->
                val updated = decodeHistory(preferences[KEY_HISTORY].orEmpty())
                    .filterNot { it.id == id }
                preferences[KEY_HISTORY] = encodeHistory(updated)
            }
        }
    }

    override suspend fun clear() {
        withContext(ioDispatcher) {
            context.historyDataStore.edit { preferences ->
                preferences[KEY_HISTORY] = "[]"
            }
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
            displayName = json.optString("displayName").takeIf { it.isNotBlank() } ?: "Processed image",
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
            warning = json.optNullableString("warning"),
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
            .putNullable("warning", image.warning)
    }

    private fun decodeImageInfo(json: JSONObject): ImageInfo? {
        val format = json.optEnum("format", ImageFormat.UNKNOWN)
        return ImageInfo(
            id = json.optString("id").takeIf { it.isNotBlank() } ?: return null,
            uriString = json.optString("uriString").takeIf { it.isNotBlank() } ?: return null,
            displayName = json.optString("displayName").takeIf { it.isNotBlank() } ?: "Original image",
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

    private fun outputExists(path: String): Boolean {
        return runCatching {
            val uri = path.toUri()
            if (uri.scheme == "content") {
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

    private companion object {
        val KEY_HISTORY = stringPreferencesKey("history_json")
        const val MAX_HISTORY_ITEMS = 200
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
