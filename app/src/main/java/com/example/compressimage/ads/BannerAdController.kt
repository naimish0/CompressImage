package com.example.compressimage.ads

import com.google.android.gms.ads.AdRequest
import kotlinx.coroutines.flow.StateFlow

interface BannerAdController {
    val uiState: StateFlow<AdsUiState>

    fun shouldShow(
        placement: BannerPlacement,
        state: AdsUiState,
    ): Boolean

    fun adUnitIdFor(placement: BannerPlacement): String

    fun buildAdRequest(): AdRequest
}
