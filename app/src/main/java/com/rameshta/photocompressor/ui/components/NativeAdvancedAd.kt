package com.rameshta.photocompressor.ui.components

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
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
import com.rameshta.photocompressor.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.rameshta.photocompressor.ads.BannerAdController
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToInt

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
    var loadState by remember(adUnitId) { mutableStateOf(NativeAdLoadState.Loading) }
    var nativeAd by remember(adUnitId) { mutableStateOf<NativeAd?>(null) }

    DisposableEffect(adUnitId) {
        active.set(true)
        onDispose {
            active.set(false)
        }
    }

    DisposableEffect(nativeAd) {
        val adToDispose = nativeAd
        onDispose {
            adToDispose?.destroy()
        }
    }

    LaunchedEffect(adUnitId, adsState.canRequestAds, adsState.initialized) {
        if (!adsState.canRequestAds || !adsState.initialized) return@LaunchedEffect
        loadState = NativeAdLoadState.Loading
        nativeAd = null

        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { loadedAd ->
                if (!active.get()) {
                    loadedAd.destroy()
                    return@forNativeAd
                }
                nativeAd = loadedAd
                loadState = NativeAdLoadState.Loaded
            }
            .withAdListener(
                object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        if (!active.get()) return
                        nativeAd = null
                        loadState = NativeAdLoadState.Failed
                    }
                },
            )
            .build()

        adLoader.loadAd(bannerAdController.buildAdRequest())
    }

    val loadedAd = nativeAd
    if (loadState != NativeAdLoadState.Loaded || loadedAd == null) return

    AndroidView(
        factory = { createNativeAdView(it) },
        update = { adView -> bindNativeAd(adView, loadedAd, colorScheme) },
        onRelease = { adView ->
            (adView.parent as? ViewGroup)?.removeView(adView)
            adView.removeAllViews()
            adView.destroy()
        },
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = MIN_INLINE_NATIVE_AD_HEIGHT_DP.dp),
    )
}

private fun createNativeAdView(context: Context): NativeAdView {
    val adView = NativeAdView(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }

    val container = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(context.dp(16), context.dp(14), context.dp(16), context.dp(16))
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
    }

    val header = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    val icon = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        visibility = View.GONE
        layoutParams = LinearLayout.LayoutParams(context.dp(44), context.dp(44))
    }

    val textColumn = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginStart = context.dp(12)
        }
    }

    val badgeRow = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    val adBadge = TextView(context).apply {
        includeFontPadding = false
        text = context.getString(R.string.ad_label)
        textSize = 11f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        setPadding(context.dp(6), context.dp(2), context.dp(6), context.dp(2))
    }

    val advertiser = TextView(context).apply {
        includeFontPadding = false
        textSize = 12f
        maxLines = 1
        ellipsize = TextUtils.TruncateAt.END
        layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply {
            marginStart = context.dp(8)
        }
    }

    val headline = TextView(context).apply {
        includeFontPadding = false
        textSize = 16f
        typeface = Typeface.DEFAULT_BOLD
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply {
            topMargin = context.dp(5)
        }
    }

    val mediaView = MediaView(context).apply {
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            context.dp(104),
        ).apply {
            topMargin = context.dp(12)
        }
    }

    val body = TextView(context).apply {
        includeFontPadding = false
        textSize = 13f
        maxLines = 2
        ellipsize = TextUtils.TruncateAt.END
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply {
            topMargin = context.dp(12)
        }
    }

    val callToAction = TextView(context).apply {
        includeFontPadding = false
        textSize = 14f
        typeface = Typeface.DEFAULT_BOLD
        gravity = Gravity.CENTER
        minHeight = context.dp(44)
        setPadding(context.dp(16), context.dp(10), context.dp(16), context.dp(10))
        layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        ).apply {
            topMargin = context.dp(14)
        }
    }

    badgeRow.addView(adBadge)
    badgeRow.addView(advertiser)
    textColumn.addView(badgeRow)
    textColumn.addView(headline)
    header.addView(icon)
    header.addView(textColumn)
    container.addView(header)
    container.addView(mediaView)
    container.addView(body)
    container.addView(callToAction)
    container.tag = NativeAdAssetViews(
        container = container,
        adBadge = adBadge,
        advertiser = advertiser,
        headline = headline,
        mediaView = mediaView,
        body = body,
        callToAction = callToAction,
        icon = icon,
    )

    adView.iconView = icon
    adView.advertiserView = advertiser
    adView.headlineView = headline
    adView.mediaView = mediaView
    adView.bodyView = body
    adView.callToActionView = callToAction
    adView.addView(container)
    return adView
}

private fun bindNativeAd(
    adView: NativeAdView,
    nativeAd: NativeAd,
    colorScheme: ColorScheme,
) {
    val views = (adView.getChildAt(0) as? LinearLayout)?.tag as? NativeAdAssetViews ?: return
    val context = adView.context

    views.container.background = GradientDrawable().apply {
        cornerRadius = context.dp(16).toFloat()
        setColor(colorScheme.surfaceContainer.toArgb())
        setStroke(context.dp(1), colorScheme.outlineVariant.copy(alpha = 0.68f).toArgb())
    }
    views.adBadge.background = GradientDrawable().apply {
        cornerRadius = context.dp(8).toFloat()
        setColor(colorScheme.secondaryContainer.toArgb())
    }
    views.adBadge.setTextColor(colorScheme.onSecondaryContainer.toArgb())
    views.advertiser.setTextColor(colorScheme.onSurfaceVariant.toArgb())
    views.headline.setTextColor(colorScheme.onSurface.toArgb())
    views.body.setTextColor(colorScheme.onSurfaceVariant.toArgb())
    views.callToAction.background = GradientDrawable().apply {
        cornerRadius = context.dp(14).toFloat()
        setColor(colorScheme.primary.toArgb())
    }
    views.callToAction.setTextColor(colorScheme.onPrimary.toArgb())

    views.headline.text = nativeAd.headline
    views.body.bindOptionalText(nativeAd.body)
    views.callToAction.bindOptionalText(nativeAd.callToAction)
    views.advertiser.bindOptionalText(nativeAd.advertiser ?: context.getString(R.string.sponsored))
    nativeAd.icon?.drawable?.let { drawable ->
        views.icon.setImageDrawable(drawable)
        views.icon.visibility = View.VISIBLE
    } ?: run {
        views.icon.visibility = View.GONE
    }
    views.mediaView.visibility = if (nativeAd.mediaContent != null) View.VISIBLE else View.GONE
    adView.setNativeAd(nativeAd)
}

private fun TextView.bindOptionalText(value: String?) {
    text = value.orEmpty()
    visibility = if (value.isNullOrBlank()) View.GONE else View.VISIBLE
}

private fun Context.dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()

private data class NativeAdAssetViews(
    val container: LinearLayout,
    val adBadge: TextView,
    val advertiser: TextView,
    val headline: TextView,
    val mediaView: MediaView,
    val body: TextView,
    val callToAction: TextView,
    val icon: ImageView,
)

private enum class NativeAdLoadState {
    Loading,
    Loaded,
    Failed,
}

private const val MIN_INLINE_NATIVE_AD_HEIGHT_DP = 220
