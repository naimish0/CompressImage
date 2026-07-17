package com.rameshta.photocompressor.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.rameshta.photocompressor.di.DefaultDispatcher
import com.rameshta.photocompressor.di.IoDispatcher
import com.rameshta.photocompressor.domain.model.BackgroundReplacementConfig
import com.rameshta.photocompressor.domain.model.CompressionConfig
import com.rameshta.photocompressor.domain.model.Dimension
import com.rameshta.photocompressor.domain.model.HistoryOperationType
import com.rameshta.photocompressor.domain.model.ImageFormat
import com.rameshta.photocompressor.domain.model.ImageInfo
import com.rameshta.photocompressor.domain.model.ProcessedImage
import com.rameshta.photocompressor.domain.model.ResizeMode
import com.rameshta.photocompressor.domain.model.SavedImage
import com.rameshta.photocompressor.domain.repository.ImageRepository
import com.rameshta.photocompressor.util.AdaptiveCompressionPlanner
import com.rameshta.photocompressor.util.ImageFormatMapper
import com.rameshta.photocompressor.util.OutputFilenameGenerator
import com.rameshta.photocompressor.util.ResizeCalculator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@Singleton
class AndroidImageRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : ImageRepository {
    private val resolver: ContentResolver = context.contentResolver

    override suspend fun loadImageInfo(uriString: String): Result<ImageInfo> = withContext(ioDispatcher) {
        runCatching {
            val uri = uriString.toUri()
            val bounds = decodeBounds(uri)
            if (bounds.width <= 0 || bounds.height <= 0) {
                throw IOException("This file is not a readable image.")
            }
            val header = readHeader(uri)

            val detectedFormat = ImageFormatMapper.fromHeader(header, bounds.mimeType)
            if (detectedFormat == ImageFormat.UNKNOWN) {
                throw IOException("Unsupported image format. Choose JPG, PNG, or WEBP.")
            }

            val orientation = readExifOrientation(uri)
            val oriented = orientBounds(bounds.width, bounds.height, orientation)
            val metadata = queryMetadata(uri)
            ImageInfo(
                id = stableImageId(uriString),
                uriString = uriString,
                displayName = metadata.displayName ?: "Selected image",
                sizeBytes = metadata.sizeBytes ?: 0L,
                width = oriented.width,
                height = oriented.height,
                format = detectedFormat,
                mimeType = detectedFormat.mimeType,
                hasAlpha = detectedFormat.supportsTransparency,
            )
        }.recoverCatching { error ->
            throw mapImageAccessError(error)
        }
    }

    override suspend fun compressImage(
        image: ImageInfo,
        config: CompressionConfig,
        progress: (Float) -> Unit,
    ): Result<ProcessedImage> = withContext(defaultDispatcher) {
        var working: Bitmap? = null
        runCatching {
            progress(0.02f)
            val targetBytes = config.targetSize.bytesOrNull()
            val targetLimitBytes = targetBytes?.let { AdaptiveCompressionPlanner.toleratedTargetBytes(it) }
            val qualityPolicy = AdaptiveCompressionPlanner.policyFor(config.compressionMode)
            val resizeResult = ResizeCalculator.calculate(image.width, image.height, config.resize)
            val outputDimension = resizeResult.dimension
                ?: throw IOException(resizeResult.validation.message ?: "Invalid output size.")
            if (!resizeResult.validation.isValid) {
                throw IOException(resizeResult.validation.message ?: "Invalid output size.")
            }
            val processingDimension = AdaptiveCompressionPlanner.fitWithin(
                dimension = outputDimension,
                maxLongSide = qualityPolicy.maxInitialLongSide,
            )

            val decoded = decodeForProcessing(
                uri = image.uriString.toUri(),
                targetDimension = processingDimension,
            )
            progress(0.18f)

            val oriented = applyExifOrientation(decoded, image.uriString.toUri())
            working = if (oriented.width != processingDimension.width || oriented.height != processingDimension.height) {
                val scaleTarget = noUpscaleTarget(oriented, processingDimension)
                val scaled = if (oriented.width != scaleTarget.width || oriented.height != scaleTarget.height) {
                    oriented.scale(scaleTarget.width, scaleTarget.height)
                } else {
                    oriented
                }
                if (scaled !== oriented) oriented.recycle()
                scaled
            } else {
                oriented
            }
            currentCoroutineContext().ensureActive()
            progress(0.35f)

            if (config.outputFormat == ImageFormat.JPEG && working.hasAlpha()) {
                val flattened = flattenTransparency(working, config.jpegBackgroundColor)
                working.recycle()
                working = flattened
            }

            val encoded = encodeWithTarget(
                bitmap = working,
                format = config.outputFormat,
                targetBytes = targetBytes,
                policy = qualityPolicy,
                progress = { local -> progress(0.35f + local * 0.55f) },
            )
            progress(0.92f)

            val outputFile = withContext(ioDispatcher) {
                val file = createUniqueOutputFile(image.displayName, config.outputFormat)
                FileOutputStream(file).use { it.write(encoded.bytes) }
                file
            }
            val validatedBounds = withContext(ioDispatcher) { validateOutputFile(outputFile) }

            progress(1f)
            ProcessedImage(
                id = UUID.randomUUID().toString(),
                original = image,
                filePath = outputFile.absolutePath,
                displayName = outputFile.name,
                sizeBytes = outputFile.length(),
                width = validatedBounds.outWidth,
                height = validatedBounds.outHeight,
                format = config.outputFormat,
                mimeType = config.outputFormat.mimeType,
                requestedTargetBytes = targetBytes,
                targetLimitBytes = targetLimitBytes,
                targetReached = targetBytes?.let { AdaptiveCompressionPlanner.targetReached(outputFile.length(), it) },
                outputQuality = encoded.quality,
                compressionMode = config.compressionMode,
                operationType = operationTypeFor(image, config),
                warning = buildWarning(
                    outputSize = outputFile.length(),
                    targetBytes = targetBytes,
                    encoded = encoded,
                    outputFormat = config.outputFormat,
                ),
            )
        }.also {
            working?.recycle()
        }
    }

    override suspend fun replaceBackground(
        image: ProcessedImage,
        config: BackgroundReplacementConfig,
        progress: (Float) -> Unit,
    ): Result<ProcessedImage> = withContext(defaultDispatcher) {
        var source: Bitmap? = null
        var output: Bitmap? = null
        runCatching {
            progress(0.1f)
            source = BitmapFactory.decodeFile(image.filePath)
                ?: throw IOException("The processed image is no longer available.")
            currentCoroutineContext().ensureActive()

            val color = config.colorArgb
            val decodedSource = source
            output = if (color == null) {
                decodedSource
            } else {
                createBitmap(decodedSource.width, decodedSource.height).also { canvasBitmap ->
                    val canvas = Canvas(canvasBitmap)
                    canvas.drawColor(color)
                    canvas.drawBitmap(decodedSource, 0f, 0f, null)
                }
            }
            progress(0.45f)

            val exportFormat = if (color == null && !config.outputFormat.supportsTransparency) {
                ImageFormat.PNG
            } else {
                config.outputFormat
            }
            val renderedOutput = output
            val safeOutput = if (exportFormat == ImageFormat.JPEG && renderedOutput.hasAlpha()) {
                flattenTransparency(renderedOutput, Color.WHITE)
            } else {
                renderedOutput
            }
            val encoded = encodeBitmap(safeOutput, exportFormat, 95)
            val file = createUniqueOutputFile(image.displayName, exportFormat, suffix = "background")
            FileOutputStream(file).use { it.write(encoded) }
            if (safeOutput !== renderedOutput) {
                safeOutput.recycle()
            }
            progress(1f)

            ProcessedImage(
                id = UUID.randomUUID().toString(),
                original = image.original,
                filePath = file.absolutePath,
                displayName = file.name,
                sizeBytes = file.length(),
                width = renderedOutput.width,
                height = renderedOutput.height,
                format = exportFormat,
                mimeType = exportFormat.mimeType,
                operationType = HistoryOperationType.BACKGROUND_REMOVED,
            )
        }.also {
            if (output !== source) output?.recycle()
            source?.recycle()
        }
    }

    override suspend fun saveImage(
        image: ProcessedImage,
        requestedName: String?,
    ): Result<SavedImage> = withContext(ioDispatcher) {
        runCatching {
            val source = File(image.filePath)
            if (!source.exists()) throw IOException("The processed image is no longer available.")
            val displayName = requestedName
                ?.takeIf { it.isNotBlank() }
                ?.let { OutputFilenameGenerator.ensureExtension(it, image.format) }
                ?: image.displayName

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Images.Media.MIME_TYPE, image.mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(
                        MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + File.separator + "Photo Compressor",
                    )
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val uri = resolver.insert(collection, values)
                ?: throw IOException("Could not create a MediaStore entry.")

            try {
                resolver.openOutputStream(uri)?.use { output ->
                    source.inputStream().use { input -> input.copyTo(output) }
                } ?: throw IOException("Could not open the save destination.")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                }
            } catch (error: Throwable) {
                resolver.delete(uri, null, null)
                throw error
            }
            SavedImage(uriString = uri.toString(), displayName = displayName)
        }
    }

    override suspend fun saveAll(images: List<ProcessedImage>): List<Result<SavedImage>> {
        return images.map { saveImage(it) }
    }

    override suspend fun cleanupObsoleteTempFiles(): Unit = withContext(ioDispatcher) {
        val cutoff = System.currentTimeMillis() - TEMP_FILE_TTL_MILLIS
        outputDir().listFiles()?.forEach { file ->
            if (file.isFile && file.lastModified() < cutoff) {
                file.delete()
            }
        }
    }

    private fun decodeBounds(uri: Uri): Bounds {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        openImageInputStream(uri).use { BitmapFactory.decodeStream(it, null, options) }
        return Bounds(options.outWidth, options.outHeight, options.outMimeType)
    }

    private fun decodeForProcessing(
        uri: Uri,
        targetDimension: Dimension,
    ): Bitmap {
        val bounds = decodeBounds(uri)
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inSampleSize = calculateInSampleSize(bounds.width, bounds.height, targetDimension.width, targetDimension.height)
        }
        return openImageInputStream(uri).use { input ->
            BitmapFactory.decodeStream(input, null, options)
        } ?: throw IOException("This image could not be decoded.")
    }

    private fun calculateInSampleSize(
        width: Int,
        height: Int,
        requestedWidth: Int,
        requestedHeight: Int,
    ): Int {
        var sampleSize = 1
        if (height > requestedHeight || width > requestedWidth) {
            var halfHeight = height / 2
            var halfWidth = width / 2
            while ((halfHeight / sampleSize) >= requestedHeight && (halfWidth / sampleSize) >= requestedWidth) {
                sampleSize *= 2
            }
        }
        return sampleSize.coerceAtLeast(1)
    }

    private fun applyExifOrientation(bitmap: Bitmap, uri: Uri): Bitmap {
        val orientation = readExifOrientation(uri)
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.preScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.preScale(-1f, 1f)
            }
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
            if (it !== bitmap) bitmap.recycle()
        }
    }

    private fun readExifOrientation(uri: Uri): Int {
        return runCatching {
            openImageInputStream(uri).use { input ->
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            }
        }.getOrDefault(ExifInterface.ORIENTATION_NORMAL)
    }

    private fun readHeader(uri: Uri): ByteArray {
        return openImageInputStream(uri).use { input ->
            ByteArray(16).also { buffer ->
                val bytesRead = input.read(buffer)
                if (bytesRead <= 0) {
                    throw IOException("This image could not be read.")
                }
            }
        }
    }

    private fun openImageInputStream(uri: Uri): InputStream {
        var accessError: Throwable? = null
        runCatching { resolver.openInputStream(uri) }
            .onSuccess { stream -> stream?.let { return it } }
            .onFailure { accessError = it }
        runCatching {
            resolver.openTypedAssetFileDescriptor(uri, "image/*", null)?.createInputStream()
        }
            .onSuccess { stream -> stream?.let { return it } }
            .onFailure { accessError = it }
        accessError?.let { throw it }
        throw FileNotFoundException("No readable stream for selected image.")
    }

    private fun mapImageAccessError(error: Throwable): Throwable {
        return when (error) {
            is SecurityException -> IOException("Photo access was denied. Select the image again.", error)
            is FileNotFoundException -> IOException("Image is unavailable. If it is stored in the cloud, download it locally or pick it from Files.", error)
            else -> error
        }
    }

    private fun orientBounds(width: Int, height: Int, orientation: Int): Dimension {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_ROTATE_270,
            ExifInterface.ORIENTATION_TRANSPOSE,
            ExifInterface.ORIENTATION_TRANSVERSE,
            -> Dimension(height, width)
            else -> Dimension(width, height)
        }
    }

    private suspend fun encodeWithTarget(
        bitmap: Bitmap,
        format: ImageFormat,
        targetBytes: Long?,
        policy: com.rameshta.photocompressor.util.CompressionQualityPolicy,
        progress: (Float) -> Unit,
    ): EncodedImage {
        if (targetBytes == null) {
            progress(1f)
            val quality = if (format == ImageFormat.PNG) null else policy.defaultQuality
            return EncodedImage(
                bytes = encodeBitmap(bitmap, format, quality ?: 100),
                quality = quality,
                scaleSteps = 0,
                targetReached = null,
            )
        }

        if (format == ImageFormat.PNG) {
            return encodePngWithProgressiveResize(bitmap, targetBytes, policy, progress)
        }

        var current = bitmap
        var ownsCurrent = false
        var scaleSteps = 0
        var best = binarySearchQuality(current, format, targetBytes, policy)
        progress(0.2f)

        while (best.targetReached != true && scaleSteps < policy.maxResizeSteps) {
            currentCoroutineContext().ensureActive()
            val nextDimension = AdaptiveCompressionPlanner.nextReducedDimension(
                dimension = Dimension(current.width, current.height),
                resizeFactor = policy.resizeFactor,
                minShortSide = policy.minOutputShortSide,
            ) ?: break
            val scaled = current.scale(nextDimension.width, nextDimension.height)
            if (ownsCurrent) current.recycle()
            current = scaled
            ownsCurrent = true
            scaleSteps += 1
            val candidate = binarySearchQuality(current, format, targetBytes, policy)
            if (candidate.targetReached == true || candidate.bytes.size < best.bytes.size) {
                best = candidate
            }
            progress(0.2f + (scaleSteps / policy.maxResizeSteps.toFloat()) * 0.75f)
        }

        if (ownsCurrent) current.recycle()
        progress(1f)
        return best.copy(scaleSteps = scaleSteps)
    }

    private suspend fun encodePngWithProgressiveResize(
        bitmap: Bitmap,
        targetBytes: Long,
        policy: com.rameshta.photocompressor.util.CompressionQualityPolicy,
        progress: (Float) -> Unit,
    ): EncodedImage {
        var current = bitmap
        var ownsCurrent = false
        var bestBytes = encodeBitmap(current, ImageFormat.PNG, 100)
        var steps = 0
        while (!AdaptiveCompressionPlanner.targetReached(bestBytes.size.toLong(), targetBytes) && steps < policy.maxResizeSteps) {
            currentCoroutineContext().ensureActive()
            val nextDimension = AdaptiveCompressionPlanner.nextReducedDimension(
                dimension = Dimension(current.width, current.height),
                resizeFactor = policy.resizeFactor,
                minShortSide = policy.minOutputShortSide,
            ) ?: break
            val scaled = current.scale(nextDimension.width, nextDimension.height)
            if (ownsCurrent) current.recycle()
            current = scaled
            ownsCurrent = true
            val bytes = encodeBitmap(current, ImageFormat.PNG, 100)
            if (bytes.size < bestBytes.size) {
                bestBytes = bytes
            }
            steps += 1
            progress((steps / policy.maxResizeSteps.toFloat()).coerceIn(0f, 1f))
        }
        if (ownsCurrent) current.recycle()
        progress(1f)
        return EncodedImage(
            bytes = bestBytes,
            quality = null,
            scaleSteps = steps,
            targetReached = AdaptiveCompressionPlanner.targetReached(bestBytes.size.toLong(), targetBytes),
        )
    }

    private suspend fun binarySearchQuality(
        bitmap: Bitmap,
        format: ImageFormat,
        targetBytes: Long,
        policy: com.rameshta.photocompressor.util.CompressionQualityPolicy,
    ): EncodedImage {
        val candidate = AdaptiveCompressionPlanner.searchHighestQuality(
            targetBytes = targetBytes,
            minQuality = policy.minAcceptableQuality,
            maxQuality = AdaptiveCompressionPlanner.MAX_QUALITY,
        ) { quality ->
            encodeBitmap(bitmap, format, quality)
        }
        return EncodedImage(
            bytes = candidate.bytes,
            quality = candidate.quality,
            scaleSteps = 0,
            targetReached = candidate.targetReached,
        ).also {
            currentCoroutineContext().ensureActive()
        }
    }

    private fun encodeBitmap(bitmap: Bitmap, format: ImageFormat, quality: Int): ByteArray {
        val output = ByteArrayOutputStream()
        val compressFormat = when (format) {
            ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
            ImageFormat.PNG -> Bitmap.CompressFormat.PNG
            ImageFormat.WEBP -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Bitmap.CompressFormat.WEBP_LOSSY
            } else {
                @Suppress("DEPRECATION")
                Bitmap.CompressFormat.WEBP
            }
            ImageFormat.UNKNOWN -> Bitmap.CompressFormat.JPEG
        }
        if (!bitmap.compress(compressFormat, quality.coerceIn(0, 100), output)) {
            throw IOException("Could not encode the image.")
        }
        return output.toByteArray()
    }

    private fun flattenTransparency(bitmap: Bitmap, backgroundColor: Int): Bitmap {
        val flattened = createBitmap(bitmap.width, bitmap.height)
        val canvas = Canvas(flattened)
        canvas.drawColor(backgroundColor)
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        return flattened
    }

    private fun buildWarning(
        outputSize: Long,
        targetBytes: Long?,
        encoded: EncodedImage,
        outputFormat: ImageFormat,
    ): String? {
        if (targetBytes == null) return null
        if (outputFormat == ImageFormat.PNG) {
            return if (encoded.targetReached == true) {
                "PNG is lossless, so size reduction was performed through safe resizing."
            } else {
                "PNG is lossless. Best quality result created, but the exact target could not be reached without significant resolution loss."
            }
        }
        if (encoded.targetReached != true) {
            return "Best quality result created. The exact target could not be reached without significant quality loss."
        }
        if (encoded.quality != null && encoded.scaleSteps >= 4) {
            return "Target reached by reducing resolution while preserving acceptable encoder quality."
        }
        return null
    }

    private fun operationTypeFor(
        image: ImageInfo,
        config: CompressionConfig,
    ): HistoryOperationType {
        return when {
            config.outputFormat != image.format -> HistoryOperationType.FORMAT_CONVERTED
            config.resize.mode != ResizeMode.ORIGINAL -> HistoryOperationType.RESIZED
            else -> HistoryOperationType.COMPRESSED
        }
    }

    private fun validateOutputFile(outputFile: File): BitmapFactory.Options {
        val validatedBounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(outputFile.absolutePath, validatedBounds)
        if (validatedBounds.outWidth <= 0 || validatedBounds.outHeight <= 0) {
            outputFile.delete()
            throw IOException("The compressed image could not be validated.")
        }
        return validatedBounds
    }

    private fun noUpscaleTarget(bitmap: Bitmap, targetDimension: Dimension): Dimension {
        return Dimension(
            width = min(bitmap.width, targetDimension.width),
            height = min(bitmap.height, targetDimension.height),
        )
    }

    private fun queryMetadata(uri: Uri): Metadata {
        var displayName: String? = null
        var sizeBytes: Long? = null
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) displayName = cursor.getString(nameIndex)
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) sizeBytes = cursor.getLong(sizeIndex)
            }
        }
        return Metadata(displayName, sizeBytes)
    }

    private fun createUniqueOutputFile(
        originalName: String,
        format: ImageFormat,
        suffix: String = "compressed",
    ): File {
        val dir = outputDir()
        var candidate = File(dir, OutputFilenameGenerator.generate(originalName, format, suffix))
        var index = 1
        while (candidate.exists()) {
            val name = candidate.nameWithoutExtension
            candidate = File(dir, "${name}_$index.${format.extension}")
            index += 1
        }
        return candidate
    }

    private fun outputDir(): File {
        return File(context.cacheDir, "processed").apply { mkdirs() }
    }

    private fun stableImageId(uriString: String): String = uriString.hashCode().toString()

    private data class Metadata(
        val displayName: String?,
        val sizeBytes: Long?,
    )

    private data class Bounds(
        val width: Int,
        val height: Int,
        val mimeType: String?,
    )

    private data class EncodedImage(
        val bytes: ByteArray,
        val quality: Int?,
        val scaleSteps: Int,
        val targetReached: Boolean?,
    )

    private companion object {
        const val TEMP_FILE_TTL_MILLIS = 7L * 24L * 60L * 60L * 1000L
    }
}
