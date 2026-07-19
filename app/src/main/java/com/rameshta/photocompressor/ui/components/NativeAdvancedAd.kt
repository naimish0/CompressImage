package com.rameshta.photocompressor.ui.components

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ColorScheme
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
import java.util.concurrent.atomic.AtomicBoolean

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
    val colorScheme = MaterialTheme.colorScheme
    val adUnitId = bannerAdController.nativeAdUnitId()
    val active = remember(adUnitId) { AtomicBoolean(true) }
    var ad by remember(adUnitId) { mutableStateOf<NativeAd?>(null) }
    DisposableEffect(adUnitId) {
        active.set(true)
        onDispose { active.set(false) }
    }
    DisposableEffect(ad) {
        val loaded = ad
        onDispose { loaded?.destroy() }
    }
    LaunchedEffect(adUnitId, adsState.canRequestAds, adsState.initialized) {
        if (!adsState.canRequestAds || !adsState.initialized) return@LaunchedEffect
        ad = null
        AdLoader.Builder(context, adUnitId)
            .forNativeAd { loaded ->
                if (active.get()) {
                    ad = loaded
                } else {
                    loaded.destroy()
                }
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    if (active.get()) ad = null
                }
            })
            .build()
            .loadAd(bannerAdController.buildAdRequest())
    }
    val loadedAd = ad ?: return
    AndroidView(
        factory = { createNativeAdView(it) },
        update = { bindNativeAd(it, loadedAd, colorScheme) },
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
    val header = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }
    val icon = ImageView(context).apply { visibility = View.GONE }
    val headline = TextView(context).apply { typeface = Typeface.DEFAULT_BOLD; maxLines = 2 }
    val advertiser = TextView(context).apply { maxLines = 1 }
    val adBadge = TextView(context).apply {
        text = context.getString(R.string.ad_label)
        textSize = 11f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        minWidth = 15.dp(context)
        minHeight = 15.dp(context)
        setPadding(6.dp(context), 2.dp(context), 6.dp(context), 2.dp(context))
    }
    val adChoices = AdChoicesView(context)
    val media = MediaView(context)
    val body = TextView(context).apply { maxLines = 2 }
    val cta = TextView(context).apply {
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        minHeight = 44.dp(context)
        setPadding(16.dp(context), 10.dp(context), 16.dp(context), 10.dp(context))
    }
    header.addView(icon, LinearLayout.LayoutParams(44.dp(context), 44.dp(context)))
    val text = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
    }
    val attributionRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }
    attributionRow.addView(adBadge)
    attributionRow.addView(
        adChoices,
        LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply { marginStart = 8.dp(context) },
    )
    text.addView(attributionRow)
    text.addView(advertiser)
    text.addView(headline)
    header.addView(
        text,
        LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginStart = 12.dp(context)
        },
    )
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
    view.tag = Assets(root, adBadge, icon, headline, advertiser, media, body, cta)
    return view
}

private fun bindNativeAd(
    view: NativeAdView,
    ad: NativeAd,
    colorScheme: ColorScheme,
) {
    val assets = view.tag as Assets
    val context = view.context
    assets.root.background = GradientDrawable().apply {
        cornerRadius = 16.dp(context).toFloat()
        setColor(colorScheme.surfaceContainer.toArgb())
        setStroke(1.dp(context), colorScheme.outlineVariant.toArgb())
    }
    assets.adBadge.background = GradientDrawable().apply {
        cornerRadius = 6.dp(context).toFloat()
        setColor(colorScheme.secondaryContainer.toArgb())
    }
    assets.adBadge.setTextColor(colorScheme.onSecondaryContainer.toArgb())
    assets.headline.setTextColor(colorScheme.onSurface.toArgb())
    assets.advertiser.setTextColor(colorScheme.onSurfaceVariant.toArgb())
    assets.body.setTextColor(colorScheme.onSurfaceVariant.toArgb())
    assets.cta.background = GradientDrawable().apply {
        cornerRadius = 12.dp(context).toFloat()
        setColor(colorScheme.primary.toArgb())
    }
    assets.cta.setTextColor(colorScheme.onPrimary.toArgb())
    assets.headline.text = ad.headline
    assets.advertiser.text = ad.advertiser.orEmpty()
    assets.advertiser.visibility = if (ad.advertiser.isNullOrBlank()) View.GONE else View.VISIBLE
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
    val root: LinearLayout,
    val adBadge: TextView,
    val icon: ImageView,
    val headline: TextView,
    val advertiser: TextView,
    val media: MediaView,
    val body: TextView,
    val cta: TextView,
)

private fun Int.dp(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()
