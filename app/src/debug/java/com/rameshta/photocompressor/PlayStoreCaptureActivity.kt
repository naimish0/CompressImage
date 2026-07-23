package com.rameshta.photocompressor

import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.gms.ads.AdRequest
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
import com.rameshta.photocompressor.ui.BackgroundUiState
import com.rameshta.photocompressor.ui.BatchItemStatus
import com.rameshta.photocompressor.ui.BatchItemUiState
import com.rameshta.photocompressor.ui.BatchUiState
import com.rameshta.photocompressor.ui.PhotoCompressorUiState
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
import java.io.File

/**
 * Debug-only deterministic renderer for Play Store screenshots.
 *
 * It invokes the production Composables with registered, rights-cleared sample
 * files and genuine outputs produced by the app. Ads are disabled by contract.
 */
class PlayStoreCaptureActivity : ComponentActivity() {
    private val captureScreen = mutableStateOf(SCREEN_HOME)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
        hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        applyCaptureOrientation(intent)
        val evidence = CaptureEvidence.prepare(this)
        captureScreen.value = intent.captureScreen()
        setContent {
            CompositionLocalProvider(
                LocalActivityResultRegistryOwner provides CaptureActivityResultRegistryOwner,
            ) {
                CompressImageTheme(dynamicColor = false) {
                    CaptureScreen(screen = captureScreen.value, evidence = evidence)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        applyCaptureOrientation(intent)
        captureScreen.value = intent.captureScreen()
    }

    private fun applyCaptureOrientation(intent: Intent) {
        requestedOrientation = if (intent.getBooleanExtra(EXTRA_LANDSCAPE, false)) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

    private fun Intent.captureScreen(): String =
        getStringExtra(EXTRA_SCREEN).orEmpty().ifBlank { SCREEN_HOME }

    companion object {
        const val EXTRA_SCREEN = "screen"
        const val EXTRA_LANDSCAPE = "landscape"
        const val SCREEN_HOME = "home"
        const val SCREEN_EDITOR = "editor"
        const val SCREEN_BATCH = "batch"
        const val SCREEN_RESULT = "result"
        const val SCREEN_BACKGROUND = "background"
        const val SCREEN_HISTORY = "history"
        const val SCREEN_SETTINGS = "settings"
    }
}

@Composable
private fun CaptureScreen(
    screen: String,
    evidence: CaptureEvidence,
) {
    val state = evidence.state
    when (screen) {
        PlayStoreCaptureActivity.SCREEN_HOME -> HomeScreen(
            state = state,
            bannerAdController = CaptureBannerAdController,
            fullScreenAdVisible = false,
            onAddImages = {},
            onRemoveImage = {},
            onOpenEditor = {},
            onOpenHistory = {},
            onOpenSettings = {},
            onExternalPickerOpened = {},
        )

        PlayStoreCaptureActivity.SCREEN_EDITOR -> EditorScreen(
            state = state,
            bannerAdController = CaptureBannerAdController,
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

        PlayStoreCaptureActivity.SCREEN_BATCH -> BatchProgressScreen(
            state = state,
            bannerAdController = CaptureBannerAdController,
            fullScreenAdVisible = false,
            onBack = {},
            onCancel = {},
            onRetryFailed = {},
            onViewResults = {},
        )

        PlayStoreCaptureActivity.SCREEN_RESULT -> ResultScreen(
            state = state,
            bannerAdController = CaptureBannerAdController,
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

        PlayStoreCaptureActivity.SCREEN_BACKGROUND -> BackgroundReplacementScreen(
            state = state,
            bannerAdController = CaptureBannerAdController,
            fullScreenAdVisible = false,
            onBack = {},
            onStartRemoval = {},
            onCancelRemoval = {},
            onReplaceBackground = {},
        )

        PlayStoreCaptureActivity.SCREEN_HISTORY -> HistoryScreen(
            state = HistoryUiState.Content(state.results),
            bannerAdController = CaptureBannerAdController,
            fullScreenAdVisible = false,
            onBack = {},
            onOpenItem = {},
            onShareItem = {},
            onRemoveItem = {},
            onClear = {},
            onRetry = {},
        )

        PlayStoreCaptureActivity.SCREEN_SETTINGS -> SettingsScreen(
            privacyOptionsRequired = true,
            selectedLanguageTag = null,
            bannerAdController = CaptureBannerAdController,
            fullScreenAdVisible = false,
            onChooseLanguage = {},
            onPrivacyOptions = {},
            onPrivacyPolicy = {},
            onBack = {},
        )

        else -> error("Unknown Play Store capture screen: $screen")
    }
}

private data class CaptureEvidence(
    val state: PhotoCompressorUiState,
) {
    companion object {
        fun prepare(activity: ComponentActivity): CaptureEvidence {
            val originalFiles = listOf(
                activity.copyAsset("portrait-curly-hair-original.png", "portrait-original.png"),
                activity.copyAsset("blue-rowboat-original.png", "rowboat-original.png"),
                activity.copyAsset("wildflower-still-life-original.png", "flowers-original.png"),
            )
            val outputFiles = listOf(
                activity.copyAsset(
                    "real-app-results/compression/portrait-compressed.jpg",
                    "portrait-compressed.jpg",
                ),
                activity.copyAsset(
                    "real-app-results/compression/lake-compressed.jpg",
                    "rowboat-compressed.jpg",
                ),
                activity.copyAsset(
                    "real-app-results/compression/flowers-compressed.jpg",
                    "flowers-compressed.jpg",
                ),
            )
            val transparentFile = activity.copyAsset(
                "real-app-results/background/portrait-transparent.png",
                "portrait-transparent.png",
            )
            val originals = listOf(
                original(
                    id = "portrait",
                    file = originalFiles[0],
                    displayName = "portrait.png",
                    sizeBytes = 2_683_003L,
                    width = 1122,
                    height = 1402,
                ),
                original(
                    id = "rowboat",
                    file = originalFiles[1],
                    displayName = "rowboat.png",
                    sizeBytes = 3_029_345L,
                    width = 1448,
                    height = 1086,
                ),
                original(
                    id = "flowers",
                    file = originalFiles[2],
                    displayName = "flowers.png",
                    sizeBytes = 2_946_511L,
                    width = 1448,
                    height = 1086,
                ),
            )
            val results = listOf(
                processed(
                    id = "portrait-result",
                    original = originals[0],
                    file = outputFiles[0],
                    displayName = "portrait-compressed.jpg",
                    sizeBytes = 235_719L,
                    width = 1122,
                    height = 1402,
                ),
                processed(
                    id = "rowboat-result",
                    original = originals[1],
                    file = outputFiles[1],
                    displayName = "rowboat-compressed.jpg",
                    sizeBytes = 341_188L,
                    width = 1448,
                    height = 1086,
                ),
                processed(
                    id = "flowers-result",
                    original = originals[2],
                    file = outputFiles[2],
                    displayName = "flowers-compressed.jpg",
                    sizeBytes = 300_892L,
                    width = 1448,
                    height = 1086,
                ),
            )
            val transparent = processed(
                id = "background-result",
                original = originals[0],
                file = transparentFile,
                displayName = "portrait-transparent.png",
                sizeBytes = 2_017_965L,
                width = 1122,
                height = 1402,
                format = ImageFormat.PNG,
                operationType = HistoryOperationType.BACKGROUND_REMOVED,
            )
            val batchItems = originals.zip(results).map { (original, result) ->
                BatchItemUiState(
                    imageId = original.id,
                    name = original.displayName,
                    status = BatchItemStatus.SUCCESS,
                    progress = 1f,
                    result = result,
                )
            }
            return CaptureEvidence(
                state = PhotoCompressorUiState(
                    selectedImages = originals,
                    config = CompressionConfig(
                        targetSize = TargetSize(TargetSizePreset.KB_200),
                        compressionMode = CompressionMode.BALANCED,
                        resize = ResizeConfig(mode = ResizeMode.PERCENT_75),
                        outputFormat = ImageFormat.JPEG,
                    ),
                    batch = BatchUiState(
                        isRunning = false,
                        items = batchItems,
                        currentIndex = originals.size,
                        summary = ProcessingSummary(
                            total = originals.size,
                            successful = originals.size,
                            failed = 0,
                            cancelled = false,
                        ),
                    ),
                    results = results,
                    selectedResultId = results.first().id,
                    history = results,
                    backgroundState = BackgroundUiState.Success(transparent),
                ),
            )
        }

        private fun original(
            id: String,
            file: File,
            displayName: String,
            sizeBytes: Long,
            width: Int,
            height: Int,
        ) = ImageInfo(
            id = id,
            uriString = Uri.fromFile(file).toString(),
            displayName = displayName,
            sizeBytes = sizeBytes,
            width = width,
            height = height,
            format = ImageFormat.PNG,
            mimeType = ImageFormat.PNG.mimeType,
            hasAlpha = false,
        )

        private fun processed(
            id: String,
            original: ImageInfo,
            file: File,
            displayName: String,
            sizeBytes: Long,
            width: Int,
            height: Int,
            format: ImageFormat = ImageFormat.JPEG,
            operationType: HistoryOperationType = HistoryOperationType.COMPRESSED,
        ) = ProcessedImage(
            id = id,
            original = original,
            filePath = file.absolutePath,
            displayName = displayName,
            sizeBytes = sizeBytes,
            width = width,
            height = height,
            format = format,
            mimeType = format.mimeType,
            requestedTargetBytes = 205_000L,
            targetLimitBytes = 210_000L,
            targetReached = id != "portrait-result",
            outputQuality = 86,
            compressionMode = CompressionMode.BALANCED,
            operationType = operationType,
            createdTimestamp = 1_768_482_600_000L,
        )
    }
}

private fun ComponentActivity.copyAsset(
    assetPath: String,
    outputName: String,
): File {
    val destination = File(cacheDir, "play-store-capture/$outputName")
    destination.parentFile?.mkdirs()
    if (!destination.isFile || destination.length() == 0L) {
        assets.open(assetPath).use { input ->
            destination.outputStream().use(input::copyTo)
        }
    }
    return destination
}

private object CaptureActivityResultRegistryOwner : ActivityResultRegistryOwner {
    override val activityResultRegistry: ActivityResultRegistry = object : ActivityResultRegistry() {
        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) = Unit
    }
}

private object CaptureBannerAdController : BannerAdController {
    override val uiState: StateFlow<AdsUiState> = MutableStateFlow(AdsUiState())

    override fun shouldShow(
        placement: BannerPlacement,
        state: AdsUiState,
    ): Boolean = false

    override fun adUnitIdFor(placement: BannerPlacement): String = error("Ads are disabled")

    override fun buildAdRequest(): AdRequest = error("Ads are disabled")
}
