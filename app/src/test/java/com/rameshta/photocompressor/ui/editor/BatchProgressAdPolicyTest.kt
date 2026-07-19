package com.rameshta.photocompressor.ui.editor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BatchProgressAdPolicyTest {
    @Test
    fun nativeAdIsSuppressedWhileProcessingIsActive() {
        assertFalse(
            shouldShowBatchCompletionNativeAd(
                isRunning = true,
                hasSummary = true,
                wasCancelled = false,
                itemCount = 3,
                successfulCount = 3,
            ),
        )
    }

    @Test
    fun nativeAdIsEligibleAfterSubstantiveProcessingCompletes() {
        assertTrue(
            shouldShowBatchCompletionNativeAd(
                isRunning = false,
                hasSummary = true,
                wasCancelled = false,
                itemCount = 3,
                successfulCount = 2,
            ),
        )
    }

    @Test
    fun nativeAdIsSuppressedWithoutAStableSummary() {
        assertFalse(
            shouldShowBatchCompletionNativeAd(
                isRunning = false,
                hasSummary = false,
                wasCancelled = false,
                itemCount = 3,
                successfulCount = 3,
            ),
        )
    }

    @Test
    fun nativeAdIsSuppressedForCancelledOrShortBatches() {
        assertFalse(
            shouldShowBatchCompletionNativeAd(
                isRunning = false,
                hasSummary = true,
                wasCancelled = true,
                itemCount = 3,
                successfulCount = 2,
            ),
        )
        assertFalse(
            shouldShowBatchCompletionNativeAd(
                isRunning = false,
                hasSummary = true,
                wasCancelled = false,
                itemCount = 2,
                successfulCount = 2,
            ),
        )
    }

    @Test
    fun nativeAdIsSuppressedWhenAllItemsFail() {
        assertFalse(
            shouldShowBatchCompletionNativeAd(
                isRunning = false,
                hasSummary = true,
                wasCancelled = false,
                itemCount = 3,
                successfulCount = 0,
            ),
        )
    }
}
