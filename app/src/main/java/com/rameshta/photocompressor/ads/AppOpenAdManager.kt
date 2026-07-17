package com.rameshta.photocompressor.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.rameshta.photocompressor.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import dagger.hilt.android.qualifiers.ApplicationContext
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOpenAdManager @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val configuration: AdsConfiguration,
    private val consentManager: ConsentManager,
    private val adsInitializer: AdsInitializer,
    private val fullscreenAdCoordinator: FullscreenAdCoordinator,
) : Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    private var registered = false
    private var currentActivity = WeakReference<Activity>(null)
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isShowingAd = false
    private var loadTimeMs = 0L
    private var lastShownAtMs = 0L
    private var backgroundedAtMs: Long? = null
    private var activeOperation = false
    private var suppressNextForegroundAd = false
    private var hasSeenProcessStart = false
    private var coldStartShowUntilMs = 0L

    fun register(application: Application) {
        if (registered) return
        registered = true
        application.registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    fun loadAdIfPossible() {
        if (!canRequestAppOpenAd() || isLoadingAd || isAdAvailable()) return
        isLoadingAd = true
        AppOpenAd.load(
            context,
            configuration.appOpenAdUnitId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadTimeMs = SystemClock.elapsedRealtime()
                    isLoadingAd = false
                    logDebug("App Open ad loaded.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    loadTimeMs = 0L
                    isLoadingAd = false
                    logDebug("App Open ad failed to load: ${error.code}")
                }
            },
        )
    }

    fun setActiveOperation(active: Boolean) {
        activeOperation = active
    }

    fun suppressNextForegroundAd() {
        suppressNextForegroundAd = true
        coldStartShowUntilMs = 0L
    }

    override fun onStart(owner: LifecycleOwner) {
        val coldStart = !hasSeenProcessStart
        hasSeenProcessStart = true
        if (coldStart) {
            coldStartShowUntilMs = SystemClock.elapsedRealtime() + COLD_START_SHOW_WINDOW_MS
        }
        showIfEligible(coldStart)
    }

    override fun onStop(owner: LifecycleOwner) {
        backgroundedAtMs = SystemClock.elapsedRealtime()
    }

    override fun onActivityStarted(activity: Activity) {
        updateCurrentActivity(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        updateCurrentActivity(activity)
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity.get() === activity) {
            currentActivity.clear()
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

    private fun showIfEligible(coldStart: Boolean) {
        if (!canRequestAppOpenAd()) return
        if (!isAdAvailable()) {
            loadAdIfPossible()
            return
        }
        val now = SystemClock.elapsedRealtime()
        val backgroundDurationMs = backgroundedAtMs?.let { now - it }
        if (!coldStart && (backgroundDurationMs == null || backgroundDurationMs < MIN_BACKGROUND_DURATION_MS)) {
            loadAdIfPossible()
            return
        }
        if (coldStart && now > coldStartShowUntilMs) {
            return
        }
        if (now - lastShownAtMs < APP_OPEN_COOLDOWN_MS) return
        if (activeOperation || consentManager.state.value.requestInProgress) return
        if (suppressNextForegroundAd) {
            suppressNextForegroundAd = false
            return
        }
        if (fullscreenAdCoordinator.consumeAppOpenSuppression()) return
        if (fullscreenAdCoordinator.recentlyFinishedFullscreenAd(FULLSCREEN_AD_RETURN_SUPPRESSION_MS)) return
        if (fullscreenAdCoordinator.isShowing.value || isShowingAd) return

        val activity = currentActivity.get()?.takeIf(::isValidHostActivity) ?: return
        val ad = appOpenAd ?: return
        if (!fullscreenAdCoordinator.tryAcquire()) return

        appOpenAd = null
        loadTimeMs = 0L
        coldStartShowUntilMs = 0L
        isShowingAd = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                lastShownAtMs = SystemClock.elapsedRealtime()
                logDebug("App Open ad shown.")
            }

            override fun onAdDismissedFullScreenContent() {
                logDebug("App Open ad dismissed.")
                finishShowing()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                logDebug("App Open ad failed to show: ${adError.code}")
                finishShowing()
            }

            override fun onAdClicked() {
                suppressNextForegroundAd()
                fullscreenAdCoordinator.suppressNextAppOpen()
                logDebug("App Open ad clicked.")
            }
        }
        try {
            ad.show(activity)
        } catch (error: RuntimeException) {
            logDebug("App Open show threw: ${error.javaClass.simpleName}")
            finishShowing()
        }
    }

    private fun finishShowing() {
        isShowingAd = false
        fullscreenAdCoordinator.release(suppressNextAppOpen = false)
        loadAdIfPossible()
    }

    private fun canRequestAppOpenAd(): Boolean {
        return configuration.appOpenAdsEnabled &&
            consentManager.canRequestAds() &&
            adsInitializer.state.value.initialized
    }

    private fun isAdAvailable(): Boolean {
        val ad = appOpenAd ?: return false
        val fresh = SystemClock.elapsedRealtime() - loadTimeMs < AD_MAX_AGE_MS
        if (!fresh) {
            appOpenAd = null
            loadTimeMs = 0L
        }
        return fresh && ad.adUnitId == configuration.appOpenAdUnitId
    }

    private fun updateCurrentActivity(activity: Activity) {
        if (isValidHostActivity(activity)) {
            currentActivity = WeakReference(activity)
        }
    }

    private fun isValidHostActivity(activity: Activity): Boolean {
        if (activity.isFinishing || activity.isDestroyed) return false
        return !activity.javaClass.name.startsWith(GOOGLE_AD_ACTIVITY_PREFIX)
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    companion object {
        const val MIN_BACKGROUND_DURATION_MS = 0L
        const val APP_OPEN_COOLDOWN_MS = 0L
        private const val COLD_START_SHOW_WINDOW_MS = 30_000L
        private const val FULLSCREEN_AD_RETURN_SUPPRESSION_MS = 3_000L
        private const val AD_MAX_AGE_MS = 4 * 60 * 60_000L
        private const val GOOGLE_AD_ACTIVITY_PREFIX = "com.google.android.gms.ads"
        private const val TAG = "AppOpenAds"
    }
}
