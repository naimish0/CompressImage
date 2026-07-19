package com.rameshta.photocompressor.ads

import com.google.android.gms.ads.AdRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleBannerAdController @Inject constructor(
    private val configuration: AdsConfiguration,
    private val consentManager: ConsentManager,
    private val adsInitializer: AdsInitializer,
    private val placementPolicy: AdPlacementPolicy,
) : BannerAdController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val uiState: StateFlow<AdsUiState> = combine(
        consentManager.state,
        adsInitializer.state,
    ) { consent, initialization ->
        AdsUiState(
            adsEnabled = configuration.adsEnabled,
            canRequestAds = consent.canRequestAds,
            initialized = initialization.initialized,
            privacyOptionsRequired = consent.privacyOptionsRequired,
            testMode = configuration.testMode,
        )
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = AdsUiState(
            adsEnabled = configuration.adsEnabled,
            canRequestAds = consentManager.canRequestAds(),
            initialized = adsInitializer.state.value.initialized,
            privacyOptionsRequired = consentManager.state.value.privacyOptionsRequired,
            testMode = configuration.testMode,
        ),
    )

    override fun shouldShow(
        placement: BannerPlacement,
        state: AdsUiState,
    ): Boolean {
        return state.adsEnabled &&
            state.canRequestAds &&
            state.initialized &&
            placementPolicy.isBannerEligible(placement)
    }

    override fun adUnitIdFor(placement: BannerPlacement): String {
        return when (placement) {
            BannerPlacement.TOP -> configuration.topBannerAdUnitId
            BannerPlacement.BOTTOM -> configuration.bottomBannerAdUnitId
            BannerPlacement.HOME_EMPTY_SPACE,
            BannerPlacement.RESULT_EMPTY_SPACE,
            BannerPlacement.HISTORY_INLINE,
            BannerPlacement.TEMPLATE_LIST_INLINE,
            -> configuration.inlineAdUnitId
        }
    }

    override fun shouldShowNativeAd(state: AdsUiState): Boolean =
        state.adsEnabled && state.canRequestAds && state.initialized && configuration.nativeAdsEnabled

    override fun nativeAdUnitId(): String = configuration.nativeAdUnitId

    override fun buildAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }
}
