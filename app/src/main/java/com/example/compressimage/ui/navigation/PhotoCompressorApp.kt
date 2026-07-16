package com.example.compressimage.ui.navigation

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
import com.example.compressimage.ads.InterstitialAdController
import com.example.compressimage.data.storage.ImageShareController
import com.example.compressimage.ui.PhotoCompressorViewModel
import com.example.compressimage.ui.background.BackgroundReplacementScreen
import com.example.compressimage.ui.comparison.ResultScreen
import com.example.compressimage.ui.editor.BatchProgressScreen
import com.example.compressimage.ui.editor.EditorScreen
import com.example.compressimage.ui.history.HistoryScreen
import com.example.compressimage.ui.home.HomeScreen
import com.example.compressimage.ui.settings.SettingsScreen
import com.example.compressimage.util.findActivity

@Composable
fun PhotoCompressorApp(
    viewModel: PhotoCompressorViewModel,
    interstitialAdController: InterstitialAdController,
    imageShareController: ImageShareController,
) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        interstitialAdController.preload()
    }

    LaunchedEffect(state.message) {
        state.message?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.consumeMessage()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                state = state,
                onAddImages = viewModel::addImageUris,
                onRemoveImage = viewModel::removeImage,
                onOpenEditor = { navController.navigate(Routes.EDITOR) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }
        composable(Routes.EDITOR) {
            EditorScreen(
                state = state,
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
                onBack = { navController.popBackStack() },
                onCancel = viewModel::cancelCompression,
                onRetryFailed = viewModel::retryFailedItems,
                onViewResults = {
                    navigateToResultWithAd(context, viewModel, interstitialAdController) {
                        navController.navigate(Routes.RESULT)
                    }
                },
            )
        }
        composable(Routes.RESULT) {
            ResultScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onSelectResult = viewModel::selectResult,
                onSaveSelected = viewModel::saveSelected,
                onSaveAll = viewModel::saveAllResults,
                onShareSelected = {
                    viewModel.selectedResult()?.let { image ->
                        startSafely(context) { context.startActivity(imageShareController.shareOneIntent(image)) }
                    }
                },
                onShareAll = {
                    if (state.results.isNotEmpty()) {
                        startSafely(context) { context.startActivity(imageShareController.shareManyIntent(state.results)) }
                    }
                },
                onOpenImage = {
                    viewModel.selectedResult()?.let { image ->
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
                onBack = { navController.popBackStack() },
                onStartRemoval = viewModel::removeBackground,
                onCancelRemoval = viewModel::cancelBackgroundRemoval,
                onReplaceBackground = viewModel::replaceBackground,
            )
        }
        composable(Routes.HISTORY) {
            HistoryScreen(
                history = state.history,
                onBack = { navController.popBackStack() },
                onOpenItem = { id ->
                    viewModel.openHistoryItem(id)
                    navController.navigate(Routes.RESULT)
                },
                onRemoveItem = viewModel::removeHistoryItem,
                onClear = viewModel::clearHistory,
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                keepOriginal = state.keepOriginal,
                onKeepOriginal = viewModel::updateKeepOriginal,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

private fun navigateToResultWithAd(
    context: Context,
    viewModel: PhotoCompressorViewModel,
    interstitialAdController: InterstitialAdController,
    navigate: () -> Unit,
) {
    val activity = context.findActivity()
    if (activity != null && viewModel.shouldShowInterstitialBeforeResult()) {
        interstitialAdController.showIfAvailable(activity, navigate)
    } else {
        navigate()
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
