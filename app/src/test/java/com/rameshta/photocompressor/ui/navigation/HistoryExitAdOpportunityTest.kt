package com.rameshta.photocompressor.ui.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryExitAdOpportunityTest {
    @Test
    fun contentExitShowsAdBeforeNavigatingAndOnlyOnce() {
        val opportunity = HistoryExitAdOpportunity()
        var finishAd: (() -> Unit)? = null
        var navigated = false

        assertTrue(
            opportunity.requestExit(
                currentRoute = Routes.HISTORY,
                hasEligibleSession = true,
                showAdBeforeExit = { finishAd = it },
                navigateBack = { navigated = true; true },
            ),
        )
        assertFalse(navigated)
        assertFalse(
            opportunity.requestExit(
                currentRoute = Routes.HISTORY,
                hasEligibleSession = true,
                showAdBeforeExit = {},
                navigateBack = { true },
            ),
        )

        finishAd?.invoke()

        assertTrue(navigated)
        opportunity.onRouteChanged(Routes.HOME)
    }

    @Test
    fun unengagedOrNonContentHistoryExitsImmediatelyWithoutAnAd() {
        val opportunity = HistoryExitAdOpportunity()
        var adRequested = false
        var navigated = false

        assertTrue(
            opportunity.requestExit(
                currentRoute = Routes.HISTORY,
                hasEligibleSession = false,
                showAdBeforeExit = { adRequested = true },
                navigateBack = { navigated = true; true },
            ),
        )

        assertFalse(adRequested)
        assertTrue(navigated)
    }

    @Test
    fun unrelatedRouteDoesNotCreateAnOpportunity() {
        val opportunity = HistoryExitAdOpportunity()

        assertFalse(
            opportunity.requestExit(
                currentRoute = Routes.HOME,
                hasEligibleSession = true,
                showAdBeforeExit = {},
                navigateBack = { true },
            ),
        )
    }

    @Test
    fun failedNavigationReleasesTheOneShotGuard() {
        val opportunity = HistoryExitAdOpportunity()

        assertTrue(
            opportunity.requestExit(
                currentRoute = Routes.HISTORY,
                hasEligibleSession = false,
                showAdBeforeExit = {},
                navigateBack = { false },
            ),
        )
        assertTrue(
            opportunity.requestExit(
                currentRoute = Routes.HISTORY,
                hasEligibleSession = false,
                showAdBeforeExit = {},
                navigateBack = { true },
            ),
        )
    }
}
