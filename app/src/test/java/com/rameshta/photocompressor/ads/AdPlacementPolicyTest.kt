package com.rameshta.photocompressor.ads

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdPlacementPolicyTest {
    @Test
    fun interstitialPolicyEnforcesActionsFirstOpportunityIntervalAndSessionCap() {
        var now = 1_000L
        val policy = AdPlacementPolicy(
            config = InterstitialPolicyConfig(
                successfulActionsRequired = 3,
                minimumIntervalMillis = 180_000L,
                maximumPerSession = 2,
                suppressFirstSessionAd = true,
            ),
            elapsedRealtimeMillis = { now },
        )

        assertFalse(policy.canShowInterstitial(InterstitialPlacement.HISTORY_OPENED))
        repeat(3) { policy.recordSuccessfulAction() }
        assertTrue(policy.canShowInterstitial(InterstitialPlacement.HISTORY_OPENED))
        assertFalse(policy.allowCurrentShowOpportunity(InterstitialPlacement.HISTORY_OPENED))
        assertFalse(policy.canShowInterstitial(InterstitialPlacement.HISTORY_OPENED))

        repeat(3) { policy.recordSuccessfulAction() }
        assertTrue(policy.allowCurrentShowOpportunity(InterstitialPlacement.HISTORY_OPENED))
        policy.recordInterstitialShown()

        repeat(3) { policy.recordSuccessfulAction() }
        assertFalse(policy.canShowInterstitial(InterstitialPlacement.HISTORY_OPENED))
        now += 180_000L
        assertTrue(policy.allowCurrentShowOpportunity(InterstitialPlacement.HISTORY_OPENED))
        policy.recordInterstitialShown()

        repeat(3) { policy.recordSuccessfulAction() }
        now += 180_000L
        assertFalse(policy.canShowInterstitial(InterstitialPlacement.HISTORY_OPENED))
    }
}
