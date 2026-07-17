package com.rameshta.photocompressor.util

import com.rameshta.photocompressor.domain.model.Dimension
import com.rameshta.photocompressor.domain.model.ResizeConfig
import com.rameshta.photocompressor.domain.model.ResizeMode
import kotlin.math.roundToInt

data class ResizeValidation(
    val dimension: Dimension?,
    val validation: ValidationResult,
    val upscales: Boolean = false,
)

object ResizeCalculator {
    const val MAX_DIMENSION = 12_000
    private const val MIN_DIMENSION = 16

    fun calculate(
        originalWidth: Int,
        originalHeight: Int,
        resize: ResizeConfig,
    ): ResizeValidation {
        if (originalWidth <= 0 || originalHeight <= 0) {
            return ResizeValidation(null, ValidationResult(false, "Original image dimensions are unavailable."))
        }

        val target = when (resize.mode) {
            ResizeMode.ORIGINAL -> Dimension(originalWidth, originalHeight)
            ResizeMode.PERCENT_25 -> scale(originalWidth, originalHeight, 0.25)
            ResizeMode.PERCENT_50 -> scale(originalWidth, originalHeight, 0.50)
            ResizeMode.PERCENT_75 -> scale(originalWidth, originalHeight, 0.75)
            ResizeMode.CUSTOM -> custom(originalWidth, originalHeight, resize)
                ?: return ResizeValidation(null, ValidationResult(false, "Enter valid width and height."))
        }

        if (target.width < MIN_DIMENSION || target.height < MIN_DIMENSION) {
            return ResizeValidation(target, ValidationResult(false, "Output dimensions are too small."))
        }
        if (target.width > MAX_DIMENSION || target.height > MAX_DIMENSION) {
            return ResizeValidation(target, ValidationResult(false, "Output dimensions are too large."))
        }
        val upscales = target.width > originalWidth || target.height > originalHeight
        if (upscales && !resize.allowUpscale) {
            return ResizeValidation(
                target,
                ValidationResult(false, "Upscaling is off. Enable it to make the image larger."),
                upscales = true,
            )
        }
        return ResizeValidation(target, ValidationResult(true), upscales = upscales)
    }

    fun aspectHeight(width: Int, originalWidth: Int, originalHeight: Int): Int {
        if (width <= 0 || originalWidth <= 0 || originalHeight <= 0) return 0
        return (width * (originalHeight.toDouble() / originalWidth.toDouble())).roundToInt().coerceAtLeast(1)
    }

    fun aspectWidth(height: Int, originalWidth: Int, originalHeight: Int): Int {
        if (height <= 0 || originalWidth <= 0 || originalHeight <= 0) return 0
        return (height * (originalWidth.toDouble() / originalHeight.toDouble())).roundToInt().coerceAtLeast(1)
    }

    private fun scale(width: Int, height: Int, ratio: Double): Dimension {
        return Dimension(
            width = (width * ratio).roundToInt().coerceAtLeast(1),
            height = (height * ratio).roundToInt().coerceAtLeast(1),
        )
    }

    private fun custom(
        originalWidth: Int,
        originalHeight: Int,
        resize: ResizeConfig,
    ): Dimension? {
        val width = resize.customWidth.trim().toIntOrNull()
        val height = resize.customHeight.trim().toIntOrNull()
        return when {
            width != null && height != null -> Dimension(width, height)
            resize.maintainAspectRatio && width != null -> Dimension(
                width = width,
                height = aspectHeight(width, originalWidth, originalHeight),
            )
            resize.maintainAspectRatio && height != null -> Dimension(
                width = aspectWidth(height, originalWidth, originalHeight),
                height = height,
            )
            else -> null
        }
    }
}
