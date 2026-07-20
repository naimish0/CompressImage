package com.rameshta.photocompressor

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.rameshta.photocompressor.data.repository.AndroidImageRepository
import com.rameshta.photocompressor.domain.model.CompressionConfig
import com.rameshta.photocompressor.domain.model.CompressionMode
import com.rameshta.photocompressor.domain.model.ImageFormat
import com.rameshta.photocompressor.domain.model.TargetSize
import com.rameshta.photocompressor.domain.model.TargetSizePreset
import com.rameshta.photocompressor.util.AdaptiveCompressionPlanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class CompressionRepositoryInstrumentedTest {
    @Test
    fun unrealisticTargetKeepsBalancedQualityFloor() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val input = File(context.cacheDir, "detailed-input.jpg")
        createDetailedImage(input)

        val repository = AndroidImageRepository(
            context = context,
            ioDispatcher = Dispatchers.IO,
            defaultDispatcher = Dispatchers.Default,
        )
        val info = repository.loadImageInfo(Uri.fromFile(input).toString()).getOrThrow()
        val result = repository.compressImage(
            image = info,
            config = CompressionConfig(
                targetSize = TargetSize(preset = TargetSizePreset.KB_100),
                compressionMode = CompressionMode.BALANCED,
                outputFormat = ImageFormat.JPEG,
            ),
            progress = {},
        ).getOrThrow()

        val balancedFloor = AdaptiveCompressionPlanner
            .policyFor(CompressionMode.BALANCED)
            .minAcceptableQuality
        assertNotNull(result.outputQuality)
        assertTrue(result.outputQuality!! >= balancedFloor)
        assertEquals(info.width, result.width)
        assertEquals(info.height, result.height)
        assertTrue(File(result.filePath).length() == result.sizeBytes)
    }

    private fun createDetailedImage(file: File) {
        val bitmap = Bitmap.createBitmap(2200, 1500, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 2f
            textSize = 32f
        }
        for (x in 0 until bitmap.width step 40) {
            paint.color = if ((x / 40) % 2 == 0) Color.rgb(20, 90, 160) else Color.rgb(180, 40, 40)
            canvas.drawLine(x.toFloat(), 0f, (bitmap.width - x).toFloat(), bitmap.height.toFloat(), paint)
        }
        for (y in 0 until bitmap.height step 36) {
            paint.color = Color.rgb(y % 255, 80, 120)
            canvas.drawText("Photo Compressor & BG Remover detail test $y", 24f, y.toFloat() + 30f, paint)
        }
        FileOutputStream(file).use { output ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)
        }
        bitmap.recycle()
    }
}
