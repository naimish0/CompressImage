package com.rameshta.photocompressor.util

import com.rameshta.photocompressor.domain.model.Dimension
import com.rameshta.photocompressor.domain.model.ResizeConfig
import com.rameshta.photocompressor.domain.model.ResizeMode
import kotlin.math.min
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
            return ResizeValidation(null, ValidationResult(false, ValidationMessage.ORIGINAL_DIMENSIONS_UNAVAILABLE))
        }

        val target = when (resize.mode) {
            ResizeMode.ORIGINAL -> Dimension(originalWidth, originalHeight)
            ResizeMode.PERCENT_25 -> scale(originalWidth, originalHeight, 0.25)
            ResizeMode.PERCENT_50 -> scale(originalWidth, originalHeight, 0.50)
            ResizeMode.PERCENT_75 -> scale(originalWidth, originalHeight, 0.75)
            ResizeMode.CUSTOM -> custom(originalWidth, originalHeight, resize)
                ?: return ResizeValidation(null, ValidationResult(false, ValidationMessage.ENTER_VALID_WIDTH_AND_HEIGHT))
        }

        if (target.width < MIN_DIMENSION || target.height < MIN_DIMENSION) {
            return ResizeValidation(target, ValidationResult(false, ValidationMessage.OUTPUT_DIMENSIONS_TOO_SMALL))
        }
        if (target.width > MAX_DIMENSION || target.height > MAX_DIMENSION) {
            return ResizeValidation(target, ValidationResult(false, ValidationMessage.OUTPUT_DIMENSIONS_TOO_LARGE))
        }
        val upscales = target.width > originalWidth || target.height > originalHeight
        if (upscales && !resize.allowUpscale) {
            return ResizeValidation(
                target,
                ValidationResult(false, ValidationMessage.UPSCALING_IS_OFF),
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
        val width = resize.customWidth.toLocalizedIntOrNull()
        val height = resize.customHeight.toLocalizedIntOrNull()
        return when {
            resize.maintainAspectRatio && width != null && height != null -> fitInside(
                originalWidth = originalWidth,
                originalHeight = originalHeight,
                maxWidth = width,
                maxHeight = height,
            )
            resize.maintainAspectRatio && width != null -> Dimension(
                width = width,
                height = aspectHeight(width, originalWidth, originalHeight),
            )
            resize.maintainAspectRatio && height != null -> Dimension(
                width = aspectWidth(height, originalWidth, originalHeight),
                height = height,
            )
            width != null && height != null -> Dimension(width, height)
            else -> null
        }
    }

    /**
     * Treats the two custom dimensions as a bounding box when aspect-ratio locking is enabled.
     * This keeps every image in a mixed-aspect batch proportional instead of stretching all of
     * them to the first image's exact width and height.
     */
    private fun fitInside(
        originalWidth: Int,
        originalHeight: Int,
        maxWidth: Int,
        maxHeight: Int,
    ): Dimension {
        if (maxWidth <= 0 || maxHeight <= 0) return Dimension(maxWidth, maxHeight)
        val scale = min(
            maxWidth.toDouble() / originalWidth.toDouble(),
            maxHeight.toDouble() / originalHeight.toDouble(),
        )
        return Dimension(
            width = (originalWidth * scale).roundToInt().coerceAtLeast(1),
            height = (originalHeight * scale).roundToInt().coerceAtLeast(1),
        )
    }
}
