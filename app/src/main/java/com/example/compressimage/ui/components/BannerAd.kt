package com.example.compressimage.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.viewinterop.AndroidView
import com.example.compressimage.BuildConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAd(
    modifier: Modifier = Modifier,
) {
    if (!BuildConfig.ADS_ENABLED) return
    val context = LocalContext.current
    val density = LocalDensity.current
    var visible by remember { mutableStateOf(true) }
    if (!visible) return

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val widthPx = with(density) { maxWidth.toPx() }
        val widthDp = (widthPx / density.density).toInt().coerceAtLeast(320)
        val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp)
        key(widthDp) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = {
                    AdView(it).apply {
                        setAdSize(adSize)
                        adUnitId = BuildConfig.ADMOB_BANNER_ID
                        adListener = object : AdListener() {
                            override fun onAdFailedToLoad(error: LoadAdError) {
                                visible = false
                                destroy()
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                },
                onRelease = { it.destroy() },
            )
        }
    }
}
