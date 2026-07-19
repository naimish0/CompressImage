package com.rameshta.photocompressor.backgroundremoval

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.net.toUri
import androidx.exifinterface.media.ExifInterface
import com.rameshta.photocompressor.di.DefaultDispatcher
import com.rameshta.photocompressor.di.IoDispatcher
import com.rameshta.photocompressor.domain.model.BackgroundRemovalResult
import com.rameshta.photocompressor.domain.model.BackgroundFailure
import com.rameshta.photocompressor.domain.model.HistoryOperationType
import com.rameshta.photocompressor.domain.model.ImageFormat
import com.rameshta.photocompressor.domain.model.ImageInfo
import com.rameshta.photocompressor.domain.model.ImageSource
import com.rameshta.photocompressor.domain.model.ProcessedImage
import com.rameshta.photocompressor.domain.model.ProcessingNotice
import com.rameshta.photocompressor.domain.repository.BackgroundRemovalRepository
import com.rameshta.photocompressor.util.ImageFormatMapper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.roundToInt

@Singleton
class OnDeviceBackgroundRemovalRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val engine: BackgroundRemovalEngine,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @param:DefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
) : BackgroundRemovalRepository {
    private val resolver: ContentResolver = context.contentResolver
    private val config = BackgroundRemovalConfig()

    override suspend fun removeBackground(
        input: ImageSource,
        progress: (Float) -> Unit,
    ): BackgroundRemovalResult {
        var source: Bitmap? = null
        var output: Bitmap? = null
        return try {
            progress(0.05f)
            val loaded = withContext(ioDispatcher) { loadBitmap(input) }
            source = loaded.bitmap
            progress(0.2f)
            currentCoroutineContext().ensureActive()

            val mask = engine.removeBackground(loaded.bitmap, config)
            progress(0.72f)
            currentCoroutineContext().ensureActive()

            output = withContext(defaultDispatcher) {
                compositeAlpha(loaded.bitmap, mask.alpha)
            }
            progress(0.86f)
            currentCoroutineContext().ensureActive()

            val outputFile = withContext(ioDispatcher) {
                writeTransparentPng(output, loaded.info.displayName)
            }
            progress(1f)

            BackgroundRemovalResult.Success(
                ProcessedImage(
                    id = UUID.randomUUID().toString(),
                    original = loaded.info,
                    filePath = outputFile.absolutePath,
                    displayName = outputFile.name,
                    sizeBytes = outputFile.length(),
                    width = output.width,
                    height = output.height,
                    format = ImageFormat.PNG,
                    mimeType = ImageFormat.PNG.mimeType,
                    operationType = HistoryOperationType.BACKGROUND_REMOVED,
                    warning = if (loaded.wasDownscaled) {
                        ProcessingNotice.BACKGROUND_SAFE_RESOLUTION
                    } else {
                        null
                    },
                ),
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: BackgroundRemovalError) {
            BackgroundRemovalResult.Failure(error.failure)
        } catch (error: OutOfMemoryError) {
            BackgroundRemovalResult.Failure(BackgroundFailure.OUT_OF_MEMORY)
        } catch (error: Throwable) {
            BackgroundRemovalResult.Failure(BackgroundFailure.TRY_SMALLER_IMAGE)
        } finally {
            output?.recycle()
            source?.recycle()
        }
    }

    private fun loadBitmap(input: ImageSource): LoadedBackgroundImage {
        val uri = input.uriString.toUri()
        val bounds = decodeBounds(uri)
        if (bounds.width <= 0 || bounds.height <= 0) {
            throw BackgroundRemovalError.DecodeFailed()
        }
        val header = readHeader(uri)
        val format = ImageFormatMapper.fromHeader(header, bounds.mimeType)
        if (format == ImageFormat.UNKNOWN) {
            throw BackgroundRemovalError.DecodeFailed(IOException("Unsupported image format."))
        }
        val orientation = readExifOrientation(uri)
        val orientedBounds = orientBounds(bounds.width, bounds.height, orientation)
        val sampleSize = calculateSampleSize(orientedBounds.width, orientedBounds.height, config.maximumOutputPixels)
        val decoded = decodeBitmap(uri, sampleSize)
        val oriented = applyExifOrientation(decoded, orientation)
        val safe = fitWithinMaxPixels(oriented, config.maximumOutputPixels)
        if (safe !== oriented) oriented.recycle()
        val metadata = queryMetadata(uri)
        val info = ImageInfo(
            id = input.id,
            uriString = input.uriString,
            displayName = metadata.displayName.orEmpty(),
            sizeBytes = metadata.sizeBytes ?: 0L,
            width = safe.width,
            height = safe.height,
            format = format,
            mimeType = format.mimeType,
            hasAlpha = format.supportsTransparency,
        )
        return LoadedBackgroundImage(
            info = info,
            bitmap = safe,
            wasDownscaled = sampleSize > 1 ||
                safe.width != orientedBounds.width ||
                safe.height != orientedBounds.height,
        )
    }

    private fun decodeBounds(uri: Uri): ImageBounds {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        openImageStream(uri).use { BitmapFactory.decodeStream(it, null, options) }
        return ImageBounds(options.outWidth, options.outHeight, options.outMimeType)
    }

    private fun decodeBitmap(uri: Uri, sampleSize: Int): Bitmap {
        val options = BitmapFactory.Options().apply {
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inSampleSize = sampleSize.coerceAtLeast(1)
        }
        return try {
            openImageStream(uri).use { BitmapFactory.decodeStream(it, null, options) }
                ?: throw BackgroundRemovalError.DecodeFailed()
        } catch (error: OutOfMemoryError) {
            throw BackgroundRemovalError.InsufficientMemory(error)
        }
    }

    private fun readHeader(uri: Uri): ByteArray {
        return openImageStream(uri).use { stream ->
            ByteArray(32).also { buffer ->
                val read = stream.read(buffer)
                if (read < buffer.size) {
                    buffer.fill(0, fromIndex = max(0, read))
                }
            }
        }
    }

    private fun openImageStream(uri: Uri): InputStream {
        return try {
            resolver.openInputStream(uri)
        } catch (_: SecurityException) {
            null
        } ?: try {
            resolver.openTypedAssetFileDescriptor(uri, "image/*", null)?.createInputStream()
        } catch (_: Throwable) {
            null
        } ?: throw BackgroundRemovalError.DecodeFailed(FileNotFoundException("Image is unavailable."))
    }

    private fun readExifOrientation(uri: Uri): Int {
        return try {
            openImageStream(uri).use { stream ->
                ExifInterface(stream).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL,
                )
            }
        } catch (_: Throwable) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.preScale(-1f, 1f)
                matrix.postRotate(90f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.preScale(-1f, 1f)
                matrix.postRotate(270f)
            }
            else -> return bitmap
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
            if (it !== bitmap) bitmap.recycle()
        }
    }

    private fun orientBounds(width: Int, height: Int, orientation: Int): ImageBounds {
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90,
            ExifInterface.ORIENTATION_ROTATE_270,
            ExifInterface.ORIENTATION_TRANSPOSE,
            ExifInterface.ORIENTATION_TRANSVERSE
            -> ImageBounds(height, width, null)
            else -> ImageBounds(width, height, null)
        }
    }

    private fun calculateSampleSize(width: Int, height: Int, maxPixels: Int): Int {
        var sample = 1
        while ((width / sample.toLong()) * (height / sample.toLong()) > maxPixels) {
            sample *= 2
        }
        return sample
    }

    private fun fitWithinMaxPixels(bitmap: Bitmap, maxPixels: Int): Bitmap {
        if (bitmap.width.toLong() * bitmap.height.toLong() <= maxPixels) return bitmap
        val scale = kotlin.math.sqrt(maxPixels.toDouble() / (bitmap.width.toDouble() * bitmap.height.toDouble()))
        val width = (bitmap.width * scale).roundToInt().coerceAtLeast(1)
        val height = (bitmap.height * scale).roundToInt().coerceAtLeast(1)
        return bitmap.scale(width, height, filter = true)
    }

    private fun compositeAlpha(source: Bitmap, alpha: ByteArray): Bitmap {
        if (alpha.size != source.width * source.height) {
            throw BackgroundRemovalError.TensorContractMismatch("Mask size does not match image size.")
        }
        val pixels = IntArray(alpha.size)
        source.getPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
        for (index in pixels.indices) {
            val maskAlpha = alpha[index].toInt() and 0xFF
            val originalAlpha = pixels[index] ushr 24
            val finalAlpha = (maskAlpha * originalAlpha) / 255
            pixels[index] = (finalAlpha shl 24) or (pixels[index] and 0x00FFFFFF)
        }
        return createBitmap(source.width, source.height).also {
            Canvas(it).drawColor(Color.TRANSPARENT)
            it.setPixels(pixels, 0, source.width, 0, 0, source.width, source.height)
        }
    }

    private fun writeTransparentPng(bitmap: Bitmap, originalName: String): File {
        val directory = File(context.cacheDir, "background_removal").also { it.mkdirs() }
        val base = originalName.substringBeforeLast('.')
            .ifBlank { "image" }
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .take(80)
        val file = File(directory, "${base}_background_removed_${System.currentTimeMillis()}.png")
        try {
            FileOutputStream(file).use { output ->
                if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                    throw IOException("PNG encoder failed.")
                }
            }
            val validation = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, validation)
            if (validation.outWidth <= 0 || validation.outHeight <= 0) {
                throw IOException("Created PNG could not be decoded.")
            }
            return file
        } catch (error: Throwable) {
            file.delete()
            throw BackgroundRemovalError.ExportFailed(error)
        }
    }

    private fun queryMetadata(uri: Uri): SourceMetadata {
        var displayName: String? = null
        var sizeBytes: Long? = null
        resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) displayName = cursor.getString(nameIndex)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) sizeBytes = cursor.getLong(sizeIndex)
                }
            }
        return SourceMetadata(displayName, sizeBytes)
    }

    private data class ImageBounds(val width: Int, val height: Int, val mimeType: String?)
    private data class SourceMetadata(val displayName: String?, val sizeBytes: Long?)
    private data class LoadedBackgroundImage(
        val info: ImageInfo,
        val bitmap: Bitmap,
        val wasDownscaled: Boolean,
    )
}
