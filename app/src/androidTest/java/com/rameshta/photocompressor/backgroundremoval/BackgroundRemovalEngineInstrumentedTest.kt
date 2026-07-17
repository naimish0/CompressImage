package com.rameshta.photocompressor.backgroundremoval

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BackgroundRemovalEngineInstrumentedTest {
    @Test
    fun onnxEngineRunsOfflineOnGeneratedBitmap() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val engine = OnnxBackgroundRemovalEngine(context, Dispatchers.Default)
        val bitmap = createBitmap(96, 96)
        Canvas(bitmap).apply {
            drawColor(Color.WHITE)
            drawCircle(
                48f,
                48f,
                28f,
                Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.rgb(32, 120, 220) },
            )
        }

        try {
            val mask = engine.removeBackground(bitmap, BackgroundRemovalConfig(maximumOutputPixels = 96 * 96))

            assertEquals(96, mask.width)
            assertEquals(96, mask.height)
            assertEquals(96 * 96, mask.alpha.size)
            assertTrue(mask.metadata.onnxSha256.isNotBlank())
        } finally {
            bitmap.recycle()
            engine.close()
        }
    }
}
