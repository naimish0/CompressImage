package com.rameshta.photocompressor.backgroundremoval

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

data class PreparedTensorImage(
    val buffer: FloatBuffer,
    val transform: LetterboxTransform,
)

object TensorImagePreprocessor {
    private val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
    private val std = floatArrayOf(0.229f, 0.224f, 0.225f)

    fun prepare(bitmap: Bitmap, inputSize: Int): PreparedTensorImage {
        val transform = Letterbox.calculate(bitmap.width, bitmap.height, inputSize)
        val letterboxed = createBitmap(inputSize, inputSize)
        val scaled = bitmap.scale(transform.scaledWidth, transform.scaledHeight, filter = true)
        try {
            val canvas = Canvas(letterboxed)
            canvas.drawColor(Color.BLACK)
            canvas.drawBitmap(
                scaled,
                transform.padX.toFloat(),
                transform.padY.toFloat(),
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG),
            )
            return PreparedTensorImage(
                buffer = toNchwRgbFloatBuffer(letterboxed),
                transform = transform,
            )
        } finally {
            if (scaled !== bitmap) scaled.recycle()
            letterboxed.recycle()
        }
    }

    internal fun normalizeChannel(value: Int, channel: Int): Float {
        return ((value / 255f) - mean[channel]) / std[channel]
    }

    private fun toNchwRgbFloatBuffer(bitmap: Bitmap): FloatBuffer {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        val buffer = ByteBuffer
            .allocateDirect(4 * 3 * width * height)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        for (channel in 0 until 3) {
            for (pixel in pixels) {
                val value = when (channel) {
                    0 -> (pixel shr 16) and 0xFF
                    1 -> (pixel shr 8) and 0xFF
                    else -> pixel and 0xFF
                }
                buffer.put(normalizeChannel(value, channel))
            }
        }
        buffer.rewind()
        return buffer
    }
}
