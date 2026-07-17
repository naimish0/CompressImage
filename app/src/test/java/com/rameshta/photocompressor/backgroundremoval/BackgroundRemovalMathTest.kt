package com.rameshta.photocompressor.backgroundremoval

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackgroundRemovalMathTest {
    @Test
    fun letterboxPreservesAspectRatioForWideImage() {
        val transform = Letterbox.calculate(sourceWidth = 4000, sourceHeight = 2000, inputSize = 320)

        assertEquals(320, transform.scaledWidth)
        assertEquals(160, transform.scaledHeight)
        assertEquals(0, transform.padX)
        assertEquals(80, transform.padY)
    }

    @Test
    fun smoothAlphaCreatesSoftTransitionAroundThreshold() {
        assertEquals(0f, MaskProcessor.smoothAlpha(0.3f, 0.5f, 0.2f), 0.0001f)
        assertEquals(1f, MaskProcessor.smoothAlpha(0.7f, 0.5f, 0.2f), 0.0001f)
        val middle = MaskProcessor.smoothAlpha(0.5f, 0.5f, 0.2f)
        assertTrue(middle in 0.45f..0.55f)
    }

    @Test
    fun maskNormalizationHandlesConstantMask() {
        val normalized = MaskProcessor.normalize(floatArrayOf(2f, 2f, 2f))
        assertEquals(0f, normalized[0], 0.0001f)
        assertEquals(0f, normalized[1], 0.0001f)
        assertEquals(0f, normalized[2], 0.0001f)
    }

    @Test
    fun reverseLetterboxProducesOutputSizedAlphaMask() {
        val config = BackgroundRemovalConfig(
            inputSize = 4,
            foregroundThreshold = 0.5f,
            transitionWidth = 0.2f,
            featherRadius = 0,
        )
        val raw = FloatArray(16) { it.toFloat() }
        val alpha = MaskProcessor.toAlphaMask(
            rawMask = raw,
            maskWidth = 4,
            maskHeight = 4,
            outputWidth = 4,
            outputHeight = 4,
            transform = Letterbox.calculate(4, 4, 4),
            config = config,
        )

        assertEquals(16, alpha.size)
        val brightCorner = alpha[15].toInt() and 0xFF
        val darkCorner = alpha[0].toInt() and 0xFF
        assertTrue(brightCorner > darkCorner)
    }

    @Test
    fun imagenetNormalizationUsesRgbChannelOrder() {
        val red = TensorImagePreprocessor.normalizeChannel(255, channel = 0)
        val green = TensorImagePreprocessor.normalizeChannel(255, channel = 1)
        val blue = TensorImagePreprocessor.normalizeChannel(255, channel = 2)

        assertTrue(red > 2f)
        assertTrue(green > 2f)
        assertTrue(blue > 2f)
        assertTrue(red < green)
        assertTrue(green < blue)
    }
}
