package com.rameshta.photocompressor.ui.previews

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityOptionsCompat
import com.google.android.gms.ads.AdRequest
import com.rameshta.photocompressor.R
import com.rameshta.photocompressor.ads.AdsUiState
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.ads.BannerPlacement
import com.rameshta.photocompressor.domain.model.CompressionConfig
import com.rameshta.photocompressor.domain.model.CompressionMode
import com.rameshta.photocompressor.domain.model.HistoryOperationType
import com.rameshta.photocompressor.domain.model.ImageFormat
import com.rameshta.photocompressor.domain.model.ImageInfo
import com.rameshta.photocompressor.domain.model.ProcessedImage
import com.rameshta.photocompressor.domain.model.ProcessingSummary
import com.rameshta.photocompressor.domain.model.ResizeConfig
import com.rameshta.photocompressor.domain.model.ResizeMode
import com.rameshta.photocompressor.domain.model.TargetSize
import com.rameshta.photocompressor.domain.model.TargetSizePreset
import com.rameshta.photocompressor.ui.BackgroundProcessingStage
import com.rameshta.photocompressor.ui.BackgroundUiState
import com.rameshta.photocompressor.ui.BatchItemStatus
import com.rameshta.photocompressor.ui.BatchItemUiState
import com.rameshta.photocompressor.ui.BatchUiState
import com.rameshta.photocompressor.ui.PhotoCompressorUiState
import com.rameshta.photocompressor.ui.uiText
import com.rameshta.photocompressor.ui.background.BackgroundReplacementScreen
import com.rameshta.photocompressor.ui.comparison.ResultScreen
import com.rameshta.photocompressor.ui.editor.BatchProgressScreen
import com.rameshta.photocompressor.ui.editor.EditorScreen
import com.rameshta.photocompressor.ui.history.HistoryScreen
import com.rameshta.photocompressor.ui.history.HistoryUiState
import com.rameshta.photocompressor.ui.home.HomeScreen
import com.rameshta.photocompressor.ui.settings.SettingsScreen
import com.rameshta.photocompressor.ui.theme.CompressImageTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@PreviewScreen
@Composable
private fun HomeScreenPreview() {
    PreviewApp {
        HomeScreen(
            state = selectedImagesState(),
            bannerAdController = PreviewBannerAdController,
            fullScreenAdVisible = false,
            onAddImages = {},
            onRemoveImage = {},
            onOpenEditor = {},
            onOpenHistory = {},
            onOpenSettings = {},
            onExternalPickerOpened = {},
        )
    }
}

@PreviewScreen
@Composable
private fun EmptyHomeScreenPreview() {
    PreviewApp {
        HomeScreen(
            state = PhotoCompressorUiState(),
            bannerAdController = PreviewBannerAdController,
            fullScreenAdVisible = false,
            onAddImages = {},
            onRemoveImage = {},
            onOpenEditor = {},
            onOpenHistory = {},
            onOpenSettings = {},
            onExternalPickerOpened = {},
        )
    }
}

@PreviewScreen
@Composable
private fun PreviewAndConfigureScreenPreview() {
    PreviewApp {
        EditorScreen(
            state = selectedImagesState(
                config = CompressionConfig(
                    targetSize = TargetSize(TargetSizePreset.KB_100),
                    compressionMode = CompressionMode.BALANCED,
                    resize = ResizeConfig(mode = ResizeMode.PERCENT_50),
                    outputFormat = ImageFormat.JPEG,
                ),
            ),
            bannerAdController = PreviewBannerAdController,
            fullScreenAdVisible = false,
            onBack = {},
            onTargetPreset = {},
            onCustomTarget = {},
            onCustomTargetUnit = {},
            onCompressionMode = {},
            onResizeMode = {},
            onCustomWidth = {},
            onCustomHeight = {},
            onMaintainAspect = {},
            onAllowUpscale = {},
            onFormat = {},
            onJpegBackgroundColor = {},
            onCompress = {},
            onRemoveBackground = {},
        )
    }
}

@PreviewScreen
@Composable
private fun CompressingScreenPreview() {
    PreviewApp {
        BatchProgressScreen(
            state = processingState(),
            bannerAdController = PreviewBannerAdController,
            fullScreenAdVisible = false,
            onBack = {},
            onCancel = {},
            onRetryFailed = {},
            onViewResults = {},
        )
    }
}

@PreviewScreen
@Composable
private fun BatchSummaryScreenPreview() {
    PreviewApp {
        BatchProgressScreen(
            state = summaryState(),
            bannerAdController = PreviewBannerAdController,
            fullScreenAdVisible = false,
            onBack = {},
            onCancel = {},
            onRetryFailed = {},
            onViewResults = {},
        )
    }
}

@PreviewScreen
@Composable
private fun CompareResultScreenPreview() {
    PreviewApp {
        ResultScreen(
            state = resultState(),
            bannerAdController = PreviewBannerAdController,
            fullScreenAdVisible = false,
            onBack = {},
            onSelectResult = {},
            onSaveSelected = {},
            onSaveAll = {},
            onShareSelected = {},
            onShareAll = {},
            onOpenImage = {},
            onCompressAnother = {},
        )
    }
}

@PreviewScreen
@Composable
private fun BackgroundScreenPreview() {
    PreviewApp {
        BackgroundReplacementScreen(
            state = selectedImagesState(
                backgroundState = BackgroundUiState.Success(
                    processedImage(
                        id = "transparent",
                        operationType = HistoryOperationType.BACKGROUND_REMOVED,
                        filePath = "/preview/transparent.png",
                        format = ImageFormat.PNG,
                    ),
                ),
            ),
            bannerAdController = PreviewBannerAdController,
            fullScreenAdVisible = false,
            onBack = {},
            onStartRemoval = {},
            onCancelRemoval = {},
            onReplaceBackground = {},
        )
    }
}

@PreviewScreen
@Composable
private fun BackgroundStartScreenPreview() {
    PreviewApp {
        BackgroundReplacementScreen(
            state = selectedImagesState(),
            bannerAdController = PreviewBannerAdController,
            fullScreenAdVisible = false,
            onBack = {},
            onStartRemoval = {},
            onCancelRemoval = {},
            onReplaceBackground = {},
        )
    }
}

@PreviewScreen
@Composable
private fun BackgroundProcessingScreenPreview() {
    PreviewApp {
        BackgroundReplacementScreen(
            state = selectedImagesState(
                backgroundState = BackgroundUiState.Running(
                    progress = 0.62f,
                    stage = BackgroundProcessingStage.REMOVING_BACKGROUND,
                ),
            ),
            bannerAdController = PreviewBannerAdController,
            fullScreenAdVisible = false,
            onBack = {},
            onStartRemoval = {},
            onCancelRemoval = {},
            onReplaceBackground = {},
        )
    }
}

@PreviewScreen
@Composable
private fun HistoryScreenPreview() {
    PreviewApp {
        HistoryScreen(
            state = HistoryUiState.Content(previewResults()),
            bannerAdController = PreviewBannerAdController,
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

@PreviewScreen
@Composable
private fun LoadingHistoryScreenPreview() {
    PreviewApp {
        HistoryScreen(
            state = HistoryUiState.Loading,
            bannerAdController = PreviewBannerAdController,
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

@PreviewScreen
@Composable
private fun EmptyHistoryScreenPreview() {
    PreviewApp {
        HistoryScreen(
            state = HistoryUiState.Empty,
            bannerAdController = PreviewBannerAdController,
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

@PreviewScreen
@Composable
private fun ErrorHistoryScreenPreview() {
    PreviewApp {
        HistoryScreen(
            state = HistoryUiState.Error(uiText(R.string.error_history_retry_or_remove)),
            bannerAdController = PreviewBannerAdController,
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

@PreviewScreen
@Composable
private fun SettingsScreenPreview() {
    PreviewApp {
        SettingsScreen(
            privacyOptionsRequired = true,
            selectedLanguageTag = null,
            bannerAdController = PreviewBannerAdController,
            fullScreenAdVisible = false,
            onChooseLanguage = {},
            onPrivacyOptions = {},
            onPrivacyPolicy = {},
            onBack = {},
        )
    }
}

@Composable
private fun PreviewApp(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalActivityResultRegistryOwner provides PreviewActivityResultRegistryOwner) {
        CompressImageTheme(dynamicColor = false) {
            content()
        }
    }
}

private object PreviewActivityResultRegistryOwner : ActivityResultRegistryOwner {
    override val activityResultRegistry: ActivityResultRegistry = object : ActivityResultRegistry() {
        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) = Unit
    }
}

private object PreviewBannerAdController : BannerAdController {
    override val uiState: StateFlow<AdsUiState> = MutableStateFlow(AdsUiState())

    override fun shouldShow(
        placement: BannerPlacement,
        state: AdsUiState,
    ): Boolean = false

    override fun adUnitIdFor(placement: BannerPlacement): String = "preview-ad-unit"

    override fun buildAdRequest(): AdRequest = AdRequest.Builder().build()
}

private fun selectedImagesState(
    config: CompressionConfig = CompressionConfig(),
    backgroundState: BackgroundUiState = BackgroundUiState.Idle,
): PhotoCompressorUiState {
    return PhotoCompressorUiState(
        selectedImages = previewImages(),
        config = config,
        backgroundState = backgroundState,
    )
}

private fun processingState(): PhotoCompressorUiState {
    return PhotoCompressorUiState(
        selectedImages = previewImages(),
        batch = BatchUiState(
            isRunning = true,
            currentIndex = 1,
            items = listOf(
                BatchItemUiState(
                    imageId = "image-1",
                    name = "family-trip.jpg",
                    status = BatchItemStatus.SUCCESS,
                    progress = 1f,
                    result = processedImage("result-1"),
                ),
                BatchItemUiState(
                    imageId = "image-2",
                    name = "product-photo.jpg",
                    status = BatchItemStatus.RUNNING,
                    progress = 0.58f,
                ),
                BatchItemUiState(
                    imageId = "image-3",
                    name = "receipt.png",
                    status = BatchItemStatus.QUEUED,
                ),
            ),
        ),
    )
}

private fun summaryState(): PhotoCompressorUiState {
    val results = previewResults()
    return PhotoCompressorUiState(
        selectedImages = previewImages(),
        results = results,
        selectedResultId = results.first().id,
        batch = BatchUiState(
            isRunning = false,
            currentIndex = 3,
            summary = ProcessingSummary(
                total = 3,
                successful = 2,
                failed = 1,
                cancelled = false,
            ),
            items = listOf(
                BatchItemUiState(
                    imageId = "image-1",
                    name = "family-trip.jpg",
                    status = BatchItemStatus.SUCCESS,
                    progress = 1f,
                    result = results[0],
                ),
                BatchItemUiState(
                    imageId = "image-2",
                    name = "product-photo.jpg",
                    status = BatchItemStatus.SUCCESS,
                    progress = 1f,
                    result = results[1],
                ),
                BatchItemUiState(
                    imageId = "image-3",
                    name = "receipt.png",
                    status = BatchItemStatus.FAILED,
                    error = uiText(R.string.error_could_not_read_image),
                ),
            ),
        ),
    )
}

private fun resultState(): PhotoCompressorUiState {
    val results = previewResults()
    return PhotoCompressorUiState(
        selectedImages = previewImages(),
        results = results,
        selectedResultId = results.first().id,
    )
}

private fun previewImages(): List<ImageInfo> {
    return listOf(
        imageInfo(
            id = "image-1",
            displayName = "family-trip.jpg",
            sizeBytes = 2_800_000L,
            width = 4032,
            height = 3024,
            format = ImageFormat.JPEG,
        ),
        imageInfo(
            id = "image-2",
            displayName = "product-photo.jpg",
            sizeBytes = 1_920_000L,
            width = 3000,
            height = 2000,
            format = ImageFormat.JPEG,
        ),
    )
}

private fun previewResults(): List<ProcessedImage> {
    return listOf(
        processedImage(
            id = "result-1",
            displayName = "family-trip-compressed.jpg",
            sizeBytes = 184_000L,
            width = 1920,
            height = 1440,
            operationType = HistoryOperationType.COMPRESSED,
        ),
        processedImage(
            id = "result-2",
            displayName = "product-photo-web.webp",
            sizeBytes = 132_000L,
            width = 1600,
            height = 1067,
            format = ImageFormat.WEBP,
            operationType = HistoryOperationType.FORMAT_CONVERTED,
        ),
    )
}

private fun imageInfo(
    id: String,
    displayName: String,
    sizeBytes: Long,
    width: Int,
    height: Int,
    format: ImageFormat,
): ImageInfo {
    return ImageInfo(
        id = id,
        uriString = "content://preview/$id",
        displayName = displayName,
        sizeBytes = sizeBytes,
        width = width,
        height = height,
        format = format,
        mimeType = format.mimeType,
        hasAlpha = format.supportsTransparency,
    )
}

private fun processedImage(
    id: String,
    displayName: String = "$id-compressed.jpg",
    sizeBytes: Long = 184_000L,
    width: Int = 1920,
    height: Int = 1440,
    filePath: String = "/preview/$id.jpg",
    format: ImageFormat = ImageFormat.JPEG,
    operationType: HistoryOperationType = HistoryOperationType.COMPRESSED,
): ProcessedImage {
    val original = imageInfo(
        id = "original-$id",
        displayName = displayName.substringBeforeLast(".") + "-original.jpg",
        sizeBytes = 2_800_000L,
        width = 4032,
        height = 3024,
        format = ImageFormat.JPEG,
    )
    return ProcessedImage(
        id = id,
        original = original,
        filePath = filePath,
        displayName = displayName,
        sizeBytes = sizeBytes,
        width = width,
        height = height,
        format = format,
        mimeType = format.mimeType,
        requestedTargetBytes = 200L * 1024L,
        targetLimitBytes = 210L * 1024L,
        targetReached = true,
        outputQuality = 86,
        compressionMode = CompressionMode.BALANCED,
        operationType = operationType,
        createdTimestamp = 1_762_000_000_000L,
    )
}

@Preview(
    name = "Screen",
    group = "Screens",
    showBackground = true,
    widthDp = 393,
    heightDp = 852,
)
private annotation class PreviewScreen
