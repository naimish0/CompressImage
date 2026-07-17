package com.rameshta.photocompressor

import android.content.Intent
import android.net.Uri
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
        val image = shareTestImage("share-test.jpg")

        val uri = ImageShareController(appContext).contentUriFor(image)

        assertEquals("content", uri.scheme)
        assertEquals("${BuildConfig.APPLICATION_ID}.fileprovider", uri.authority)
        appContext.contentResolver.openInputStream(uri)?.use { input ->
            assertEquals(1, input.read())
        } ?: fail("FileProvider did not return a readable stream.")
    }

    @Test
    fun shareOneIntentIncludesPreviewClipDataAndReadGrant() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val image = shareTestImage("share-preview-test.jpg")
        val controller = ImageShareController(appContext)

        val chooser = controller.shareOneIntent(image)
        val sendIntent = chooser.targetIntent()
        val streamUri = sendIntent.streamUri()

        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        assertEquals(Intent.ACTION_SEND, sendIntent.action)
        assertEquals(ImageFormat.JPEG.mimeType, sendIntent.type)
        assertEquals(image.displayName, sendIntent.getStringExtra(Intent.EXTRA_TITLE))
        assertEquals(streamUri, sendIntent.clipData?.getItemAt(0)?.uri)
        assertTrue(sendIntent.hasReadGrant())
        assertTrue(chooser.hasReadGrant())
    }

    @Test
    fun shareManyIntentIncludesClipDataForEverySharedImage() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val images = listOf(
            shareTestImage("share-many-one.jpg"),
            shareTestImage("share-many-two.jpg"),
        )
        val controller = ImageShareController(appContext)

        val chooser = controller.shareManyIntent(images)
        val sendIntent = chooser.targetIntent()
        val streamUris = sendIntent.streamUris()

        assertEquals(Intent.ACTION_CHOOSER, chooser.action)
        assertEquals(Intent.ACTION_SEND_MULTIPLE, sendIntent.action)
        assertEquals(ImageFormat.JPEG.mimeType, sendIntent.type)
        assertEquals(2, streamUris.size)
        assertEquals(2, sendIntent.clipData?.itemCount)
        assertEquals(streamUris[0], sendIntent.clipData?.getItemAt(0)?.uri)
        assertEquals(streamUris[1], sendIntent.clipData?.getItemAt(1)?.uri)
        assertTrue(sendIntent.hasReadGrant())
        assertTrue(chooser.hasReadGrant())
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

    private fun shareTestImage(fileName: String): ProcessedImage {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val outputDir = File(appContext.cacheDir, "processed").apply { mkdirs() }
        val output = File(outputDir, fileName).apply {
            writeBytes(byteArrayOf(1, 2, 3))
        }
        return ProcessedImage(
            id = fileName,
            original = ImageInfo(
                id = "original-$fileName",
                uriString = output.toURI().toString(),
                displayName = fileName,
                sizeBytes = output.length(),
                width = 1,
                height = 1,
                format = ImageFormat.JPEG,
                mimeType = ImageFormat.JPEG.mimeType,
                hasAlpha = false,
            ),
            filePath = output.absolutePath,
            displayName = fileName,
            sizeBytes = output.length(),
            width = 1,
            height = 1,
            format = ImageFormat.JPEG,
            mimeType = ImageFormat.JPEG.mimeType,
        )
    }

    @Suppress("DEPRECATION")
    private fun Intent.targetIntent(): Intent {
        return getParcelableExtra(Intent.EXTRA_INTENT)
            ?: throw AssertionError("Chooser did not include a send intent.")
    }

    @Suppress("DEPRECATION")
    private fun Intent.streamUri(): Uri {
        return getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
            ?: throw AssertionError("Send intent did not include a stream URI.")
    }

    @Suppress("DEPRECATION")
    private fun Intent.streamUris(): List<Uri> {
        return getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            ?: throw AssertionError("Send intent did not include stream URIs.")
    }

    private fun Intent.hasReadGrant(): Boolean {
        return flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0
    }
}
