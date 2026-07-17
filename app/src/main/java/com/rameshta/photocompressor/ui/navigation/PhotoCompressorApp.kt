package com.rameshta.photocompressor.ui.navigation

import android.content.ActivityNotFoundException
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rameshta.photocompressor.ads.AppOpenAdManager
import com.rameshta.photocompressor.ads.AdsInitializer
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.ads.ConsentManager
import com.rameshta.photocompressor.ads.InterstitialAdManager
import com.rameshta.photocompressor.ads.InterstitialPlacement
import com.rameshta.photocompressor.data.storage.ImageShareController
import com.rameshta.photocompressor.ui.PendingAdAction
import com.rameshta.photocompressor.ui.PhotoCompressorViewModel
import com.rameshta.photocompressor.ui.background.BackgroundReplacementScreen
import com.rameshta.photocompressor.ui.comparison.ResultScreen
import com.rameshta.photocompressor.ui.editor.BatchProgressScreen
import com.rameshta.photocompressor.ui.editor.EditorScreen
import com.rameshta.photocompressor.ui.history.HistoryScreen
import com.rameshta.photocompressor.ui.home.HomeScreen
import com.rameshta.photocompressor.ui.settings.SettingsScreen
import com.rameshta.photocompressor.util.findActivity

@Composable
fun PhotoCompressorApp(
    viewModel: PhotoCompressorViewModel,
    consentManager: ConsentManager,
    adsInitializer: AdsInitializer,
    bannerAdController: BannerAdController,
    interstitialAdManager: InterstitialAdManager,
    imageShareController: ImageShareController,
    appOpenAdManager: AppOpenAdManager,
) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val consentState by consentManager.state.collectAsStateWithLifecycle()
    val adsInitializationState by adsInitializer.state.collectAsStateWithLifecycle()
    val fullScreenAdVisible by interstitialAdManager.isFullScreenAdShowing.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        context.findActivity()?.let { activity ->
            consentManager.requestConsentInfoUpdate(activity) {
                adsInitializer.initializeIfAllowed(consentManager.canRequestAds())
                interstitialAdManager.preload()
                appOpenAdManager.loadAdIfPossible()
            }
        }
    }

    LaunchedEffect(consentState.canRequestAds) {
        if (consentState.canRequestAds) {
            adsInitializer.initializeIfAllowed(true)
            interstitialAdManager.preload()
            appOpenAdManager.loadAdIfPossible()
        }
    }

    LaunchedEffect(consentState.canRequestAds, adsInitializationState.initialized) {
        if (consentState.canRequestAds && adsInitializationState.initialized) {
            appOpenAdManager.loadAdIfPossible()
        }
    }

    LaunchedEffect(state.hasActiveProcessing) {
        appOpenAdManager.setActiveOperation(state.hasActiveProcessing)
    }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.consumeMessage()
        }
    }

    LaunchedEffect(state.pendingAdAction) {
        fun runAfterOptionalInterstitial(
            placement: InterstitialPlacement,
            action: () -> Unit,
        ) {
            val activity = context.findActivity()
            if (activity != null &&
                !state.hasActiveProcessing &&
                interstitialAdManager.canShow(placement)
            ) {
                interstitialAdManager.show(activity, placement, onFinished = action)
            } else {
                action()
            }
        }

        when (val pendingAction = state.pendingAdAction) {
            PendingAdAction.OpenHistory -> {
                fun navigateToHistory() {
                    viewModel.consumePendingAdAction()
                    if (navController.currentBackStackEntry?.destination?.route != Routes.HISTORY) {
                        navController.navigate(Routes.HISTORY)
                    }
                }
                if (navController.currentBackStackEntry?.destination?.route == Routes.HISTORY) {
                    viewModel.consumePendingAdAction()
                } else {
                    runAfterOptionalInterstitial(
                        placement = InterstitialPlacement.HISTORY_OPENED,
                        action = ::navigateToHistory,
                    )
                }
            }
            is PendingAdAction.SaveResult -> {
                runAfterOptionalInterstitial(
                    placement = InterstitialPlacement.SAVE_CLICKED,
                    action = { viewModel.performPendingSave(pendingAction.requestId) },
                )
            }
            PendingAdAction.None -> Unit
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                state = state,
                bannerAdController = bannerAdController,
                fullScreenAdVisible = fullScreenAdVisible,
                onAddImages = viewModel::addImageUris,
                onRemoveImage = viewModel::removeImage,
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
                onOpenHistory = viewModel::requestOpenHistory,
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onExternalPickerOpened = appOpenAdManager::suppressNextForegroundAd,
            )
        }
        composable(Routes.EDITOR) {
            EditorScreen(
                state = state,
                bannerAdController = bannerAdController,
                fullScreenAdVisible = fullScreenAdVisible,
                onBack = { navController.popBackStack() },
                onTargetPreset = viewModel::updateTargetPreset,
                onCustomTarget = viewModel::updateCustomTarget,
                onCustomTargetUnit = viewModel::updateCustomTargetUnit,
                onCompressionMode = viewModel::updateCompressionMode,
                onResizeMode = viewModel::updateResizeMode,
                onCustomWidth = viewModel::updateCustomWidth,
                onCustomHeight = viewModel::updateCustomHeight,
                onMaintainAspect = viewModel::updateMaintainAspectRatio,
                onAllowUpscale = viewModel::updateAllowUpscale,
                onFormat = viewModel::updateOutputFormat,
                onJpegBackgroundColor = viewModel::updateJpegBackgroundColor,
                onCompress = {
                    viewModel.startCompression()
                    navController.navigate(Routes.PROGRESS)
                },
                onRemoveBackground = {
                    viewModel.removeBackground()
                    navController.navigate(Routes.BACKGROUND)
                },
            )
        }
        composable(Routes.PROGRESS) {
            BackHandler(enabled = state.batch.isRunning) {
                Toast.makeText(context, "Cancel compression before leaving this screen.", Toast.LENGTH_SHORT).show()
            }
            BatchProgressScreen(
                state = state,
                bannerAdController = bannerAdController,
                fullScreenAdVisible = fullScreenAdVisible,
                onBack = { navController.popBackStack() },
                onCancel = viewModel::cancelCompression,
                onRetryFailed = viewModel::retryFailedItems,
                onViewResults = {
                    navController.navigate(Routes.RESULT)
                },
            )
        }
        composable(Routes.RESULT) {
            ResultScreen(
                state = state,
                bannerAdController = bannerAdController,
                fullScreenAdVisible = fullScreenAdVisible,
                onBack = { navController.popBackStack() },
                onSelectResult = viewModel::selectResult,
                onSaveSelected = viewModel::saveSelected,
                onSaveAll = viewModel::saveAllResults,
                onShareSelected = {
                    viewModel.selectedResult()?.let { image ->
                        appOpenAdManager.suppressNextForegroundAd()
                        startSafely(context) { context.startActivity(imageShareController.shareOneIntent(image)) }
                    }
                },
                onShareAll = {
                    if (state.results.isNotEmpty()) {
                        appOpenAdManager.suppressNextForegroundAd()
                        startSafely(context) { context.startActivity(imageShareController.shareManyIntent(state.results)) }
                    }
                },
                onOpenImage = {
                    viewModel.selectedResult()?.let { image ->
                        appOpenAdManager.suppressNextForegroundAd()
                        startSafely(context) { context.startActivity(imageShareController.openIntent(image)) }
                    }
                },
                onCompressAnother = {
                    viewModel.clearSelection()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(Routes.BACKGROUND) {
            BackgroundReplacementScreen(
                state = state,
                bannerAdController = bannerAdController,
                fullScreenAdVisible = fullScreenAdVisible,
                onBack = { navController.popBackStack() },
                onStartRemoval = viewModel::removeBackground,
                onCancelRemoval = viewModel::cancelBackgroundRemoval,
                onReplaceBackground = viewModel::replaceBackground,
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
                history = state.history,
                bannerAdController = bannerAdController,
                fullScreenAdVisible = fullScreenAdVisible,
                onBack = { navController.popBackStack() },
                onOpenItem = { id ->
                    viewModel.openHistoryItem(id)
                    navController.navigate(Routes.RESULT)
                },
                onShareItem = { id ->
                    state.history.firstOrNull { it.id == id }?.let { image ->
                        appOpenAdManager.suppressNextForegroundAd()
                        startSafely(context) { context.startActivity(imageShareController.shareOneIntent(image)) }
                    }
                },
                onRemoveItem = viewModel::removeHistoryItem,
                onClear = viewModel::clearHistory,
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                keepOriginal = state.keepOriginal,
                privacyOptionsRequired = consentState.privacyOptionsRequired,
                bannerAdController = bannerAdController,
                fullScreenAdVisible = fullScreenAdVisible,
                onKeepOriginal = viewModel::updateKeepOriginal,
                onPrivacyOptions = {
                    context.findActivity()?.let { activity ->
                        appOpenAdManager.suppressNextForegroundAd()
                        consentManager.showPrivacyOptionsForm(activity) {
                            adsInitializer.initializeIfAllowed(consentManager.canRequestAds())
                            interstitialAdManager.preload()
                            appOpenAdManager.loadAdIfPossible()
                        }
                    }
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}

private fun startSafely(context: Context, block: () -> Unit) {
    try {
        block()
    } catch (error: ActivityNotFoundException) {
        Toast.makeText(context, "No app can handle this action.", Toast.LENGTH_LONG).show()
    } catch (error: IllegalArgumentException) {
        Toast.makeText(context, "The image is no longer available.", Toast.LENGTH_LONG).show()
    }
}
