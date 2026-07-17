package com.rameshta.photocompressor

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rameshta.photocompressor.ads.AdsConfiguration
import com.rameshta.photocompressor.ads.AdsInitializer
import com.rameshta.photocompressor.data.storage.ImageShareController
import com.rameshta.photocompressor.domain.model.ImageFormat
import com.rameshta.photocompressor.domain.model.ImageInfo
import com.rameshta.photocompressor.domain.model.ProcessedImage
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.rameshta.photocompressor", appContext.packageName)
    }

    @Test
    fun fileProviderAuthorityUsesApplicationId() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val outputDir = File(appContext.cacheDir, "processed").apply { mkdirs() }
        val output = File(outputDir, "share-test.jpg").apply {
            writeBytes(byteArrayOf(1, 2, 3))
        }
        val image = ProcessedImage(
            id = "share-test",
            original = ImageInfo(
                id = "original-share-test",
                uriString = output.toURI().toString(),
                displayName = "share-test.jpg",
                sizeBytes = output.length(),
                width = 1,
                height = 1,
                format = ImageFormat.JPEG,
                mimeType = ImageFormat.JPEG.mimeType,
                hasAlpha = false,
            ),
            filePath = output.absolutePath,
            displayName = "share-test.jpg",
            sizeBytes = output.length(),
            width = 1,
            height = 1,
            format = ImageFormat.JPEG,
            mimeType = ImageFormat.JPEG.mimeType,
        )

        val uri = ImageShareController(appContext).contentUriFor(image)

        assertEquals("content", uri.scheme)
        assertEquals("${BuildConfig.APPLICATION_ID}.fileprovider", uri.authority)
        appContext.contentResolver.openInputStream(uri)?.use { input ->
            assertEquals(1, input.read())
        } ?: fail("FileProvider did not return a readable stream.")
    }

    @Test
    fun admobInitializerReportsInitializedWhenAdsCanBeRequested() {
        runBlocking {
            val appContext = InstrumentationRegistry.getInstrumentation().targetContext
            val initializer = AdsInitializer(appContext, AdsConfiguration())

            initializer.initializeIfAllowed(canRequestAds = true)

            withTimeout(10_000) {
                initializer.state.first { it.initialized }
            }
        }
    }
}
