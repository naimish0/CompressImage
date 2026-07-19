package com.rameshta.photocompressor.ads

import com.rameshta.photocompressor.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsConfiguration @Inject constructor() {
    val testMode: Boolean = BuildConfig.ADS_TEST_MODE
    val admobAppId: String = BuildConfig.ADMOB_APP_ID
    val topBannerAdUnitId: String = BuildConfig.TOP_BANNER_AD_UNIT_ID
    val bottomBannerAdUnitId: String = BuildConfig.BOTTOM_BANNER_AD_UNIT_ID
    val inlineAdUnitId: String = BuildConfig.INLINE_AD_UNIT_ID
    val nativeAdUnitId: String = BuildConfig.NATIVE_AD_UNIT_ID
    val historyInterstitialAdUnitId: String = BuildConfig.HISTORY_INTERSTITIAL_AD_UNIT_ID
    val saveInterstitialAdUnitId: String = BuildConfig.SAVE_INTERSTITIAL_AD_UNIT_ID
    val appOpenAdUnitId: String = BuildConfig.ADMOB_APP_OPEN_AD_UNIT_ID

    val adsEnabled: Boolean =
        BuildConfig.ADS_ENABLED &&
            admobAppId.isNotBlank() &&
            bottomBannerAdUnitId.isNotBlank() &&
            historyInterstitialAdUnitId.isNotBlank()

    val appOpenAdsEnabled: Boolean = adsEnabled && appOpenAdUnitId.isNotBlank()
    val nativeAdsEnabled: Boolean = adsEnabled && nativeAdUnitId.isNotBlank()
}

data class AdsUiState(
    val adsEnabled: Boolean = false,
    val canRequestAds: Boolean = false,
    val initialized: Boolean = false,
    val privacyOptionsRequired: Boolean = false,
    val testMode: Boolean = false,
)

data class AdsInitializationState(
    val initialized: Boolean = false,
    val initializing: Boolean = false,
)
