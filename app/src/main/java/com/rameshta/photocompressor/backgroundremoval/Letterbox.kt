package com.rameshta.photocompressor.backgroundremoval

import kotlin.math.roundToInt

data class LetterboxTransform(
    val inputSize: Int,
    val sourceWidth: Int,
    val sourceHeight: Int,
    val scaledWidth: Int,
    val scaledHeight: Int,
    val padX: Int,
    val padY: Int,
) {
    val scaleX: Float = scaledWidth.toFloat() / sourceWidth.toFloat()
    val scaleY: Float = scaledHeight.toFloat() / sourceHeight.toFloat()
}

object Letterbox {
    fun calculate(sourceWidth: Int, sourceHeight: Int, inputSize: Int): LetterboxTransform {
        require(sourceWidth > 0 && sourceHeight > 0 && inputSize > 0)
        val scale = minOf(
            inputSize.toFloat() / sourceWidth.toFloat(),
            inputSize.toFloat() / sourceHeight.toFloat(),
        )
        val scaledWidth = (sourceWidth * scale).roundToInt().coerceIn(1, inputSize)
        val scaledHeight = (sourceHeight * scale).roundToInt().coerceIn(1, inputSize)
        return LetterboxTransform(
            inputSize = inputSize,
            sourceWidth = sourceWidth,
            sourceHeight = sourceHeight,
            scaledWidth = scaledWidth,
            scaledHeight = scaledHeight,
            padX = (inputSize - scaledWidth) / 2,
            padY = (inputSize - scaledHeight) / 2,
        )
    }
}
