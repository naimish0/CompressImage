package com.rameshta.photocompressor.backgroundremoval

import kotlin.math.floor
import kotlin.math.roundToInt

object MaskProcessor {
    fun flattenModelOutput(value: Any, width: Int, height: Int): FloatArray {
        @Suppress("UNCHECKED_CAST")
        val typed = value as? Array<Array<Array<FloatArray>>>
            ?: throw BackgroundRemovalError.TensorContractMismatch("Unexpected model output value type.")
        if (typed.size != 1 || typed[0].size != 1 || typed[0][0].size != height) {
            throw BackgroundRemovalError.TensorContractMismatch("Unexpected model output dimensions.")
        }
        val output = FloatArray(width * height)
        var index = 0
        for (row in typed[0][0]) {
            if (row.size != width) {
                throw BackgroundRemovalError.TensorContractMismatch("Unexpected model output row width.")
            }
            for (valueAtPixel in row) output[index++] = valueAtPixel
        }
        return output
    }

    fun toAlphaMask(
        rawMask: FloatArray,
        maskWidth: Int,
        maskHeight: Int,
        outputWidth: Int,
        outputHeight: Int,
        transform: LetterboxTransform,
        config: BackgroundRemovalConfig,
    ): ByteArray {
        require(rawMask.size == maskWidth * maskHeight)
        val normalized = normalize(rawMask)
        val alpha = ByteArray(outputWidth * outputHeight)
        var index = 0
        for (y in 0 until outputHeight) {
            val modelY = transform.padY + ((y + 0.5f) * transform.scaledHeight / outputHeight.toFloat())
            for (x in 0 until outputWidth) {
                val modelX = transform.padX + ((x + 0.5f) * transform.scaledWidth / outputWidth.toFloat())
                val confidence = bilinear(normalized, maskWidth, maskHeight, modelX, modelY)
                val softened = smoothAlpha(confidence, config.foregroundThreshold, config.transitionWidth)
                val clamped = softened.coerceIn(config.minimumAlpha, 1f)
                alpha[index++] = (clamped * 255f).roundToInt().coerceIn(0, 255).toByte()
            }
        }
        return if (config.featherRadius > 0) feather(alpha, outputWidth, outputHeight, config.featherRadius) else alpha
    }

    fun smoothAlpha(confidence: Float, threshold: Float, transitionWidth: Float): Float {
        val start = (threshold - transitionWidth / 2f).coerceAtLeast(0f)
        val end = (threshold + transitionWidth / 2f).coerceAtMost(1f)
        if (confidence <= start) return 0f
        if (confidence >= end) return 1f
        val t = ((confidence - start) / (end - start)).coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    fun normalize(mask: FloatArray): FloatArray {
        var min = Float.POSITIVE_INFINITY
        var max = Float.NEGATIVE_INFINITY
        for (value in mask) {
            if (value < min) min = value
            if (value > max) max = value
        }
        val range = max - min
        if (!range.isFinite() || range <= 1e-6f) return FloatArray(mask.size)
        return FloatArray(mask.size) { index -> ((mask[index] - min) / range).coerceIn(0f, 1f) }
    }

    private fun bilinear(
        values: FloatArray,
        width: Int,
        height: Int,
        x: Float,
        y: Float,
    ): Float {
        val clampedX = x.coerceIn(0f, (width - 1).toFloat())
        val clampedY = y.coerceIn(0f, (height - 1).toFloat())
        val x0 = floor(clampedX).toInt()
        val y0 = floor(clampedY).toInt()
        val x1 = (x0 + 1).coerceAtMost(width - 1)
        val y1 = (y0 + 1).coerceAtMost(height - 1)
        val dx = clampedX - x0
        val dy = clampedY - y0
        val top = values[y0 * width + x0] * (1f - dx) + values[y0 * width + x1] * dx
        val bottom = values[y1 * width + x0] * (1f - dx) + values[y1 * width + x1] * dx
        return top * (1f - dy) + bottom * dy
    }

    private fun feather(alpha: ByteArray, width: Int, height: Int, radius: Int): ByteArray {
        val output = ByteArray(alpha.size)
        var index = 0
        for (y in 0 until height) {
            for (x in 0 until width) {
                var sum = 0
                var count = 0
                for (yy in (y - radius)..(y + radius)) {
                    if (yy !in 0 until height) continue
                    for (xx in (x - radius)..(x + radius)) {
                        if (xx !in 0 until width) continue
                        sum += (alpha[yy * width + xx].toInt() and 0xFF)
                        count++
                    }
                }
                output[index++] = (sum / count).coerceIn(0, 255).toByte()
            }
        }
        return output
    }
}
