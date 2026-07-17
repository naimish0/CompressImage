package com.rameshta.photocompressor.ads

import com.google.android.gms.ads.AdRequest
import kotlinx.coroutines.flow.StateFlow

interface BannerAdController {
    val uiState: StateFlow<AdsUiState>

    fun shouldShow(
        placement: BannerPlacement,
        state: AdsUiState,
    ): Boolean

    fun adUnitIdFor(placement: BannerPlacement): String

    fun shouldShowNativeAd(state: AdsUiState): Boolean {
        return state.adsEnabled &&
            state.canRequestAds &&
            state.initialized &&
            nativeAdUnitId().isNotBlank()
    }

    fun nativeAdUnitId(): String = ""

    fun buildAdRequest(): AdRequest
}
