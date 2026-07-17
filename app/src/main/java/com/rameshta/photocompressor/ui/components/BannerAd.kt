package com.rameshta.photocompressor.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.ads.BannerPlacement
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun PhotoCompressorBanner(
    placement: BannerPlacement,
    bannerAdController: BannerAdController,
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
    minScreenHeightDp: Int = 0,
    reserveSpaceWhenFailed: Boolean = false,
    instanceKey: String = placement.name,
) {
    val configuration = LocalConfiguration.current
    if (hidden || configuration.screenHeightDp < minScreenHeightDp) return

    val adsState by bannerAdController.uiState.collectAsStateWithLifecycle()
    if (!bannerAdController.shouldShow(placement, adsState)) return

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthDp = maxWidth.value.toInt().coerceAtLeast(MIN_BANNER_WIDTH_DP)
        val context = LocalContext.current
        val adUnitId = bannerAdController.adUnitIdFor(placement)
        @Suppress("DEPRECATION")
        val adSize = remember(widthDp) {
            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
        }
        val bannerHeightDp = adSize.height
            .coerceAtLeast(0)
            .coerceAtMost(MAX_BANNER_HEIGHT_DP)
        var loadState by remember(adUnitId, widthDp, instanceKey) { mutableStateOf(BannerLoadState.Loading) }
        val adView = remember(adUnitId, widthDp, instanceKey) {
            AdView(context).apply {
                this.adUnitId = adUnitId
                setAdSize(adSize)
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
            }
        }
        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(adView, lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when (event) {
                    Lifecycle.Event.ON_RESUME -> adView.resume()
                    Lifecycle.Event.ON_PAUSE -> adView.pause()
                    Lifecycle.Event.ON_DESTROY -> adView.destroy()
                    else -> Unit
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
                adView.destroy()
            }
        }

        LaunchedEffect(adView, adsState.canRequestAds, adsState.initialized, instanceKey) {
            if (!adsState.canRequestAds || !adsState.initialized) return@LaunchedEffect
            loadState = BannerLoadState.Loading
            adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    loadState = BannerLoadState.Loaded
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loadState = BannerLoadState.Failed
                }
            }
            adView.loadAd(bannerAdController.buildAdRequest())
        }

        if (loadState == BannerLoadState.Failed && !reserveSpaceWhenFailed) return@BoxWithConstraints

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeightDp.dp),
        ) {
            if (loadState != BannerLoadState.Failed) {
                AndroidView(
                    factory = { adView },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
fun TopBannerAd(
    bannerAdController: BannerAdController,
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
) {
    PhotoCompressorBanner(
        placement = BannerPlacement.TOP,
        bannerAdController = bannerAdController,
        modifier = modifier,
        hidden = hidden,
        reserveSpaceWhenFailed = true,
        instanceKey = "top",
    )
}

@Composable
fun BottomBannerAd(
    bannerAdController: BannerAdController,
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
) {
    PhotoCompressorBanner(
        placement = BannerPlacement.BOTTOM,
        bannerAdController = bannerAdController,
        modifier = modifier,
        hidden = hidden,
        reserveSpaceWhenFailed = true,
        instanceKey = "bottom",
    )
}

@Composable
fun InlineHistoryAd(
    placementKey: String,
    bannerAdController: BannerAdController,
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
) {
    PhotoCompressorBanner(
        placement = BannerPlacement.HISTORY_INLINE,
        bannerAdController = bannerAdController,
        modifier = modifier,
        hidden = hidden,
        minScreenHeightDp = MIN_INLINE_BANNER_SCREEN_HEIGHT_DP,
        reserveSpaceWhenFailed = false,
        instanceKey = placementKey,
    )
}

@Composable
fun EmptySpaceBannerAd(
    placement: BannerPlacement,
    bannerAdController: BannerAdController,
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
) {
    PhotoCompressorBanner(
        placement = placement,
        bannerAdController = bannerAdController,
        modifier = modifier,
        hidden = hidden,
        minScreenHeightDp = MIN_EMPTY_SPACE_BANNER_SCREEN_HEIGHT_DP,
        reserveSpaceWhenFailed = false,
        instanceKey = "empty-${placement.name}",
    )
}

@Composable
fun AdScreenScaffold(
    bannerAdController: BannerAdController,
    fullScreenAdVisible: Boolean,
    showTopBanner: Boolean = true,
    showBottomBanner: Boolean = true,
    hasBottomContent: Boolean = false,
    topBar: @Composable () -> Unit = {},
    bottomContent: @Composable ColumnScope.() -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
    val adsState by bannerAdController.uiState.collectAsStateWithLifecycle()
    val showTopAd = showTopBanner &&
        !fullScreenAdVisible &&
        bannerAdController.shouldShow(BannerPlacement.TOP, adsState)
    val showBottomAd = showBottomBanner &&
        !fullScreenAdVisible &&
        bannerAdController.shouldShow(BannerPlacement.BOTTOM, adsState)
    val showBottomBar = showBottomAd || hasBottomContent

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = topBar,
        bottomBar = {
            if (showBottomBar) {
                Column(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (showBottomAd) {
                        BottomBannerAd(
                            bannerAdController = bannerAdController,
                            hidden = false,
                        )
                    }
                    bottomContent()
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
        ) {
            if (showTopAd) {
                TopBannerAd(
                    bannerAdController = bannerAdController,
                    hidden = false,
                )
            }
            Box(modifier = Modifier.fillMaxSize()) {
                content(PaddingValues())
            }
        }
    }
}

private enum class BannerLoadState {
    Loading,
    Loaded,
    Failed,
}

private const val MAX_BANNER_HEIGHT_DP = 120

private const val MIN_BANNER_WIDTH_DP = 320
private const val MIN_INLINE_BANNER_SCREEN_HEIGHT_DP = 600
private const val MIN_EMPTY_SPACE_BANNER_SCREEN_HEIGHT_DP = 620
