package com.rameshta.photocompressor.ads

import android.app.Activity
import com.google.android.gms.ads.AdRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeConsentManager(
    initialState: ConsentUiState = ConsentUiState(canRequestAds = true),
) : ConsentManager {
    private val mutableState = MutableStateFlow(initialState)
    override val state: StateFlow<ConsentUiState> = mutableState

    override fun canRequestAds(): Boolean = mutableState.value.canRequestAds

    override fun requestConsentInfoUpdate(
        activity: Activity,
        onCanRequestAds: () -> Unit,
    ) {
        if (canRequestAds()) onCanRequestAds()
    }

    override fun showPrivacyOptionsForm(
        activity: Activity,
        onDismissed: () -> Unit,
    ) {
        onDismissed()
    }

    fun updateState(state: ConsentUiState) {
        mutableState.value = state
    }
}

class FakeBannerAdController(
    initialState: AdsUiState = AdsUiState(
        adsEnabled = true,
        canRequestAds = true,
        initialized = true,
        testMode = true,
    ),
) : BannerAdController {
    private val mutableState = MutableStateFlow(initialState)
    override val uiState: StateFlow<AdsUiState> = mutableState
    val requestedPlacements = mutableListOf<BannerPlacement>()

    override fun shouldShow(
        placement: BannerPlacement,
        state: AdsUiState,
    ): Boolean {
        requestedPlacements += placement
        return state.adsEnabled && state.canRequestAds && state.initialized
    }

    override fun adUnitIdFor(placement: BannerPlacement): String = "fake-banner"

    override fun buildAdRequest(): AdRequest = AdRequest.Builder().build()

    fun updateState(state: AdsUiState) {
        mutableState.value = state
    }
}

class FakeInterstitialAdManager : InterstitialAdManager {
    private val mutableFullScreenAdShowing = MutableStateFlow(false)
    override val isFullScreenAdShowing: StateFlow<Boolean> = mutableFullScreenAdShowing.asStateFlow()
    val shownPlacements = mutableListOf<InterstitialPlacement>()
    var preloadCalls: Int = 0
        private set
    var canShow: Boolean = true

    override fun preload(placement: InterstitialPlacement?) {
        preloadCalls += 1
    }

    override fun recordSuccessfulAction() = Unit

    override fun canShow(placement: InterstitialPlacement): Boolean = canShow

    override fun show(
        activity: Activity,
        placement: InterstitialPlacement,
        onFinished: () -> Unit,
    ) {
        shownPlacements += placement
        onFinished()
    }

}
