package com.rameshta.photocompressor.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.rameshta.photocompressor.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleInterstitialAdManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val configuration: AdsConfiguration,
    private val consentManager: ConsentManager,
    private val adsInitializer: AdsInitializer,
    private val placementPolicy: AdPlacementPolicy,
    private val fullscreenAdCoordinator: FullscreenAdCoordinator,
) : InterstitialAdManager {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val interstitialAds = mutableMapOf<InterstitialPlacement, InterstitialAd>()
    private val loadingPlacements = mutableSetOf<InterstitialPlacement>()
    private var isShowing = false

    override val isFullScreenAdShowing: StateFlow<Boolean> = fullscreenAdCoordinator.isShowing

    override fun preload(placement: InterstitialPlacement?) {
        val placements = placement?.let(::listOf) ?: InterstitialPlacement.entries
        placements.forEach(::preloadPlacement)
    }

    override fun canShow(placement: InterstitialPlacement): Boolean {
        return placementPolicy.isInterstitialEligible(placement) &&
            canLoadAd() &&
            interstitialAds[placement] != null &&
            placement !in loadingPlacements &&
            !isShowing &&
            !fullscreenAdCoordinator.isShowing.value
    }

    override fun show(
        activity: Activity,
        placement: InterstitialPlacement,
        onFinished: () -> Unit,
    ) {
        if (!canShow(placement) || !isActivityForeground(activity)) {
            onFinished()
            return
        }
        scope.launch {
            showLoadedAd(activity, placement, onFinished)
        }
    }

    private fun preloadPlacement(placement: InterstitialPlacement) {
        if (!canLoadAd() ||
            isShowing ||
            placement in loadingPlacements ||
            interstitialAds[placement] != null
        ) {
            return
        }
        loadingPlacements += placement
        InterstitialAd.load(
            context,
            adUnitIdFor(placement),
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    logDebug("${placement.name} interstitial loaded.")
                    loadingPlacements -= placement
                    interstitialAds[placement] = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    logDebug("${placement.name} interstitial failed to load: ${error.code}")
                    loadingPlacements -= placement
                    interstitialAds.remove(placement)
                }
            },
        )
    }

    private fun showLoadedAd(
        activity: Activity,
        placement: InterstitialPlacement,
        onFinished: () -> Unit,
    ) {
        val ad = interstitialAds.remove(placement)
        if (ad == null || isShowing || !isActivityForeground(activity) || !fullscreenAdCoordinator.tryAcquire()) {
            preload(placement)
            onFinished()
            return
        }
        isShowing = true
        var finished = false
        fun finishOnce() {
            if (finished) return
            finished = true
            onFinished()
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                logDebug("${placement.name} interstitial shown.")
            }

            override fun onAdDismissedFullScreenContent() {
                logDebug("${placement.name} interstitial dismissed.")
                isShowing = false
                fullscreenAdCoordinator.release(suppressNextAppOpen = false)
                preload(placement)
                finishOnce()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                logDebug("${placement.name} interstitial failed to show: ${adError.code}")
                isShowing = false
                fullscreenAdCoordinator.release(suppressNextAppOpen = false)
                preload(placement)
                finishOnce()
            }

            override fun onAdImpression() {
                logDebug("${placement.name} interstitial impression.")
            }

            override fun onAdClicked() {
                fullscreenAdCoordinator.suppressNextAppOpen()
                logDebug("${placement.name} interstitial clicked.")
            }
        }
        try {
            ad.show(activity)
        } catch (error: RuntimeException) {
            logDebug("Interstitial show threw: ${error.javaClass.simpleName}")
            isShowing = false
            fullscreenAdCoordinator.release(suppressNextAppOpen = false)
            preload(placement)
            finishOnce()
        }
    }

    private fun canLoadAd(): Boolean {
        return configuration.adsEnabled &&
            consentManager.canRequestAds() &&
            adsInitializer.state.value.initialized
    }

    private fun adUnitIdFor(placement: InterstitialPlacement): String {
        return when (placement) {
            InterstitialPlacement.HISTORY_OPENED -> configuration.historyInterstitialAdUnitId
            InterstitialPlacement.SAVE_CLICKED -> configuration.saveInterstitialAdUnitId
        }
    }

    private fun isActivityForeground(activity: Activity): Boolean {
        if (activity.isFinishing || activity.isDestroyed) return false
        val lifecycleOwner = activity as? LifecycleOwner ?: return true
        return lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    private companion object {
        const val TAG = "InterstitialAds"
    }
}
