package com.rameshta.photocompressor.ui.components

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.AdChoicesView
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.rameshta.photocompressor.R
import com.rameshta.photocompressor.ads.BannerAdController

@Composable
fun InlineNativeAdvancedAd(
    bannerAdController: BannerAdController,
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
) {
    if (hidden) return
    val adsState by bannerAdController.uiState.collectAsStateWithLifecycle()
    if (!bannerAdController.shouldShowNativeAd(adsState)) return
    val context = LocalContext.current
    val adUnitId = bannerAdController.nativeAdUnitId()
    var ad by remember(adUnitId) { mutableStateOf<NativeAd?>(null) }
    DisposableEffect(ad) {
        val loaded = ad
        onDispose { loaded?.destroy() }
    }
    LaunchedEffect(adUnitId, adsState.canRequestAds, adsState.initialized) {
        if (!adsState.canRequestAds || !adsState.initialized) return@LaunchedEffect
        ad = null
        AdLoader.Builder(context, adUnitId)
            .forNativeAd { loaded -> ad = loaded }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) { ad = null }
            })
            .build()
            .loadAd(bannerAdController.buildAdRequest())
    }
    val loadedAd = ad ?: return
    AndroidView(
        factory = { createNativeAdView(it) },
        update = { bindNativeAd(it, loadedAd) },
        onRelease = { view ->
            (view.parent as? ViewGroup)?.removeView(view)
            view.destroy()
        },
        modifier = modifier.fillMaxWidth().heightIn(min = 220.dp),
    )
}

private fun createNativeAdView(context: Context): NativeAdView {
    val view = NativeAdView(context)
    val root = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(16.dp(context), 14.dp(context), 16.dp(context), 16.dp(context))
    }
    val header = LinearLayout(context)
    val icon = ImageView(context).apply { visibility = View.GONE }
    val headline = TextView(context).apply { typeface = Typeface.DEFAULT_BOLD; maxLines = 2 }
    val advertiser = TextView(context).apply { maxLines = 1 }
    val adChoices = AdChoicesView(context)
    val media = MediaView(context)
    val body = TextView(context).apply { maxLines = 2 }
    val cta = TextView(context).apply { typeface = Typeface.DEFAULT_BOLD; minHeight = 44.dp(context) }
    header.addView(icon, LinearLayout.LayoutParams(44.dp(context), 44.dp(context)))
    val text = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    text.addView(adChoices)
    text.addView(advertiser)
    text.addView(headline)
    header.addView(text, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
    root.addView(header)
    // Keep enough room for video creatives; short MediaViews trigger the SDK validator warning.
    root.addView(media, LinearLayout.LayoutParams(-1, 180.dp(context)))
    root.addView(body)
    root.addView(cta)
    view.addView(root)
    view.iconView = icon
    view.headlineView = headline
    view.advertiserView = advertiser
    view.adChoicesView = adChoices
    view.mediaView = media
    view.bodyView = body
    view.callToActionView = cta
    view.tag = Assets(icon, headline, advertiser, media, body, cta)
    return view
}

private fun bindNativeAd(view: NativeAdView, ad: NativeAd) {
    val assets = view.tag as Assets
    assets.headline.text = ad.headline
    assets.advertiser.text = ad.advertiser.orEmpty()
    assets.body.text = ad.body.orEmpty()
    assets.body.visibility = if (ad.body.isNullOrBlank()) View.GONE else View.VISIBLE
    assets.cta.text = ad.callToAction.orEmpty()
    assets.cta.visibility = if (ad.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE
    ad.icon?.drawable?.let { assets.icon.setImageDrawable(it); assets.icon.visibility = View.VISIBLE }
        ?: run { assets.icon.visibility = View.GONE }
    assets.media.mediaContent = ad.mediaContent
    assets.media.visibility = if (ad.mediaContent == null) View.GONE else View.VISIBLE
    view.setNativeAd(ad)
}

private data class Assets(
    val icon: ImageView,
    val headline: TextView,
    val advertiser: TextView,
    val media: MediaView,
    val body: TextView,
    val cta: TextView,
)

private fun Int.dp(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()
