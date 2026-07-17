package com.rameshta.photocompressor.ui.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.ads.AdRequest
import com.rameshta.photocompressor.ads.AdsUiState
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.ads.BannerPlacement
import com.rameshta.photocompressor.domain.model.ImageFormat
import com.rameshta.photocompressor.domain.model.ImageInfo
import com.rameshta.photocompressor.domain.model.ProcessedImage
import com.rameshta.photocompressor.ui.theme.CompressImageTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val bannerAdController = NoAdsBannerAdController()

    @Test
    fun loadingStateRendersVisibleHistoryUi() {
        setHistoryContent(HistoryUiState.Loading)

        composeRule.onNodeWithText("History").assertIsDisplayed()
        composeRule.onNodeWithText("Loading history...").assertIsDisplayed()
    }

    @Test
    fun emptyStateRendersVisibleHistoryUi() {
        setHistoryContent(HistoryUiState.Empty)

        composeRule.onNodeWithText("History").assertIsDisplayed()
        composeRule.onNodeWithText("No processed images yet.").assertIsDisplayed()
    }

    @Test
    fun contentStateRendersVisibleHistoryUi() {
        val item = processedImage("history-item")

        setHistoryContent(HistoryUiState.Content(listOf(item)))

        composeRule.onNodeWithText("History").assertIsDisplayed()
        composeRule.onNodeWithText(item.displayName).assertIsDisplayed()
        composeRule.onNodeWithText("Open").assertIsDisplayed()
    }

    @Test
    fun errorStateRendersVisibleHistoryUi() {
        setHistoryContent(HistoryUiState.Error("History unavailable"))

        composeRule.onNodeWithText("History").assertIsDisplayed()
        composeRule.onNodeWithText("History could not be loaded.").assertIsDisplayed()
        composeRule.onNodeWithText("Retry").assertIsDisplayed()
    }

    private fun setHistoryContent(state: HistoryUiState) {
        composeRule.setContent {
            CompressImageTheme {
                HistoryScreen(
                    state = state,
                    bannerAdController = bannerAdController,
                    fullScreenAdVisible = false,
                    onBack = {},
                    onOpenItem = {},
                    onShareItem = {},
                    onRemoveItem = {},
                    onClear = {},
                    onRetry = {},
                )
            }
        }
    }
}

private class NoAdsBannerAdController : BannerAdController {
    override val uiState: StateFlow<AdsUiState> = MutableStateFlow(AdsUiState())

    override fun shouldShow(
        placement: BannerPlacement,
        state: AdsUiState,
    ): Boolean = false

    override fun adUnitIdFor(placement: BannerPlacement): String = "unused"

    override fun buildAdRequest(): AdRequest = AdRequest.Builder().build()
}

private fun processedImage(id: String): ProcessedImage {
    val original = ImageInfo(
        id = id,
        uriString = "uri://$id",
        displayName = "$id.png",
        sizeBytes = 1_000,
        width = 100,
        height = 100,
        format = ImageFormat.PNG,
        mimeType = ImageFormat.PNG.mimeType,
        hasAlpha = true,
    )
    return ProcessedImage(
        id = id,
        original = original,
        filePath = "/tmp/$id.png",
        displayName = "$id.png",
        sizeBytes = 500,
        width = 100,
        height = 100,
        format = ImageFormat.PNG,
        mimeType = ImageFormat.PNG.mimeType,
    )
}
