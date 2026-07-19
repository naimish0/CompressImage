package com.rameshta.photocompressor.ads

import android.os.SystemClock
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
    WORKFLOW_COMPLETED,
}

data class InterstitialPolicyConfig(
    val successfulActionsRequired: Int = 0,
    val minimumIntervalMillis: Long = 3 * 60 * 1000L,
    val maximumPerSession: Int = 3,
    val suppressFirstSessionAd: Boolean = false,
)

@Singleton
class AdPlacementPolicy @Inject constructor() {
    private var interstitialConfig = InterstitialPolicyConfig()
    private var elapsedRealtimeMillis: () -> Long = SystemClock::elapsedRealtime
    private var successfulActions = 0
    private var interstitialsShownThisSession = 0
    private var lastInterstitialShownAtMillis: Long? = null
    private var firstEligibleOpportunitySuppressed = false

    internal constructor(
        config: InterstitialPolicyConfig,
        elapsedRealtimeMillis: () -> Long,
    ) : this() {
        this.interstitialConfig = config
        this.elapsedRealtimeMillis = elapsedRealtimeMillis
    }

    fun isBannerEligible(placement: BannerPlacement): Boolean {
        return when (placement) {
            BannerPlacement.TOP,
            BannerPlacement.BOTTOM,
            BannerPlacement.HOME_EMPTY_SPACE,
            BannerPlacement.RESULT_EMPTY_SPACE,
            -> true
            BannerPlacement.HISTORY_INLINE,
            BannerPlacement.TEMPLATE_LIST_INLINE,
            -> false
        }
    }

    fun isInterstitialEligible(placement: InterstitialPlacement): Boolean {
        return when (placement) {
            InterstitialPlacement.WORKFLOW_COMPLETED -> true
        }
    }

    @Synchronized
    fun recordSuccessfulAction() {
        successfulActions = (successfulActions + 1).coerceAtMost(interstitialConfig.successfulActionsRequired)
    }

    @Synchronized
    fun canShowInterstitial(placement: InterstitialPlacement): Boolean {
        if (!isInterstitialEligible(placement)) return false
        if (interstitialConfig.successfulActionsRequired > 0 &&
            successfulActions < interstitialConfig.successfulActionsRequired
        ) return false
        if (interstitialConfig.maximumPerSession > 0 &&
            interstitialsShownThisSession >= interstitialConfig.maximumPerSession
        ) return false

        val lastShownAt = lastInterstitialShownAtMillis
        return interstitialConfig.minimumIntervalMillis <= 0L ||
            lastShownAt == null ||
            elapsedRealtimeMillis() - lastShownAt >= interstitialConfig.minimumIntervalMillis
    }

    /**
     * Consumes only the deliberately suppressed first opportunity. A real impression is
     * committed separately so failed-to-show ads do not count against the caps.
     */
    @Synchronized
    fun allowCurrentShowOpportunity(placement: InterstitialPlacement): Boolean {
        if (!canShowInterstitial(placement)) return false
        if (interstitialConfig.suppressFirstSessionAd && !firstEligibleOpportunitySuppressed) {
            firstEligibleOpportunitySuppressed = true
            successfulActions = 0
            return false
        }
        return true
    }

    @Synchronized
    fun recordInterstitialShown() {
        interstitialsShownThisSession += 1
        successfulActions = 0
        lastInterstitialShownAtMillis = elapsedRealtimeMillis()
    }
}
