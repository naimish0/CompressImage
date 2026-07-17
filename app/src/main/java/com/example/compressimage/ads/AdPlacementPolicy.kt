package com.example.compressimage.ads

import javax.inject.Inject
import javax.inject.Singleton

enum class BannerPlacement {
    TOP,
    BOTTOM,
    HOME_EMPTY_SPACE,
    RESULT_EMPTY_SPACE,
    HISTORY_INLINE,
    TEMPLATE_LIST_INLINE,
}

enum class InterstitialPlacement {
    HISTORY_OPENED,
    SAVE_CLICKED,
}

data class InterstitialPolicyConfig(
    val successfulActionsRequired: Int = 3,
    val minimumIntervalMillis: Long = 3 * 60 * 1000L,
    val maximumPerSession: Int = 3,
    val suppressFirstSessionAd: Boolean = true,
)

@Singleton
class AdPlacementPolicy @Inject constructor() {
    fun isBannerEligible(placement: BannerPlacement): Boolean {
        return when (placement) {
            BannerPlacement.TOP,
            BannerPlacement.BOTTOM,
            BannerPlacement.HOME_EMPTY_SPACE,
            BannerPlacement.RESULT_EMPTY_SPACE,
            BannerPlacement.HISTORY_INLINE,
            BannerPlacement.TEMPLATE_LIST_INLINE,
            -> true
        }
    }

    fun isInterstitialEligible(placement: InterstitialPlacement): Boolean {
        return when (placement) {
            InterstitialPlacement.HISTORY_OPENED,
            InterstitialPlacement.SAVE_CLICKED,
            -> true
        }
    }
}
