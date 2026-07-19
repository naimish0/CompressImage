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

        assertFalse(policy.canShowInterstitial(InterstitialPlacement.WORKFLOW_COMPLETED))
        repeat(3) { policy.recordSuccessfulAction() }
        assertTrue(policy.canShowInterstitial(InterstitialPlacement.WORKFLOW_COMPLETED))
        assertFalse(policy.allowCurrentShowOpportunity(InterstitialPlacement.WORKFLOW_COMPLETED))
        assertFalse(policy.canShowInterstitial(InterstitialPlacement.WORKFLOW_COMPLETED))

        repeat(3) { policy.recordSuccessfulAction() }
        assertTrue(policy.allowCurrentShowOpportunity(InterstitialPlacement.WORKFLOW_COMPLETED))
        policy.recordInterstitialShown()

        repeat(3) { policy.recordSuccessfulAction() }
        assertFalse(policy.canShowInterstitial(InterstitialPlacement.WORKFLOW_COMPLETED))
        now += 180_000L
        assertTrue(policy.allowCurrentShowOpportunity(InterstitialPlacement.WORKFLOW_COMPLETED))
        policy.recordInterstitialShown()

        repeat(3) { policy.recordSuccessfulAction() }
        now += 180_000L
        assertFalse(policy.canShowInterstitial(InterstitialPlacement.WORKFLOW_COMPLETED))
    }

    @Test
    fun historyExitAndWorkflowPlacementsShareTheFrequencyPolicy() {
        var now = 1_000L
        val policy = AdPlacementPolicy(
            config = InterstitialPolicyConfig(
                successfulActionsRequired = 0,
                minimumIntervalMillis = 180_000L,
                maximumPerSession = 2,
                suppressFirstSessionAd = false,
            ),
            elapsedRealtimeMillis = { now },
        )

        assertTrue(policy.allowCurrentShowOpportunity(InterstitialPlacement.HISTORY_EXITED))
        policy.recordInterstitialShown()

        assertFalse(policy.canShowInterstitial(InterstitialPlacement.WORKFLOW_COMPLETED))
        now += 180_000L
        assertTrue(policy.canShowInterstitial(InterstitialPlacement.WORKFLOW_COMPLETED))
    }
}
