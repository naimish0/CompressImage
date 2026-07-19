package com.rameshta.photocompressor.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.rameshta.photocompressor.BuildConfig
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
    private var backgroundedLocaleTags: String? = null
    private var localeRecreationSourceActivityId: Int? = null
    private var activeOperation = false
    private var suppressNextForegroundAd = false
    private var hasSeenProcessStart = false
    private var adsShownThisSession = 0

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
    }

    fun suppressForLocaleRecreation(activity: Activity) {
        localeRecreationSourceActivityId = System.identityHashCode(activity)
    }

    override fun onStart(owner: LifecycleOwner) {
        val coldStart = !hasSeenProcessStart
        hasSeenProcessStart = true
        val backgroundedAt = backgroundedAtMs
        backgroundedAtMs = null
        val localeChangedWhileBackgrounded = backgroundedLocaleTags?.let { previousLocaleTags ->
            previousLocaleTags != currentLocaleTags()
        } == true
        backgroundedLocaleTags = null
        val localeRecreationPending = localeRecreationSourceActivityId != null
        localeRecreationSourceActivityId = null
        showIfEligible(
            coldStart = coldStart,
            backgroundedAt = backgroundedAt,
            suppressForLocaleChange = localeChangedWhileBackgrounded || localeRecreationPending,
        )
    }

    override fun onStop(owner: LifecycleOwner) {
        backgroundedAtMs = SystemClock.elapsedRealtime()
        backgroundedLocaleTags = currentLocaleTags()
    }

    override fun onActivityStarted(activity: Activity) {
        updateCurrentActivity(activity)
    }

    override fun onActivityResumed(activity: Activity) {
        updateCurrentActivity(activity)
        val sourceActivityId = localeRecreationSourceActivityId
        if (sourceActivityId != null && sourceActivityId != System.identityHashCode(activity)) {
            localeRecreationSourceActivityId = null
        }
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

    private fun showIfEligible(
        coldStart: Boolean,
        backgroundedAt: Long?,
        suppressForLocaleChange: Boolean,
    ) {
        if (!canRequestAppOpenAd()) return
        val now = SystemClock.elapsedRealtime()
        if (suppressNextForegroundAd) {
            suppressNextForegroundAd = false
            loadAdIfPossible()
            return
        }
        if (suppressForLocaleChange) {
            loadAdIfPossible()
            return
        }
        if (fullscreenAdCoordinator.consumeAppOpenSuppression()) {
            loadAdIfPossible()
            return
        }
        if (!isAdAvailable()) {
            loadAdIfPossible()
            return
        }
        if (activeOperation || consentManager.state.value.requestInProgress) return
        if (fullscreenAdCoordinator.recentlyFinishedFullscreenAd(FULLSCREEN_AD_RETURN_SUPPRESSION_MS)) return
        if (fullscreenAdCoordinator.isShowing.value || isShowingAd) return

        val activity = currentActivity.get()?.takeIf(::isValidHostActivity) ?: return
        val ad = appOpenAd ?: return
        if (!fullscreenAdCoordinator.tryAcquire()) return

        appOpenAd = null
        loadTimeMs = 0L
        isShowingAd = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                lastShownAtMs = SystemClock.elapsedRealtime()
                adsShownThisSession += 1
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

    private fun currentLocaleTags(): String {
        return ContextCompat.getContextForLanguage(context)
            .resources
            .configuration
            .locales
            .toLanguageTags()
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
        private const val FULLSCREEN_AD_RETURN_SUPPRESSION_MS = 3_000L
        private const val AD_MAX_AGE_MS = 4 * 60 * 60_000L
        private const val GOOGLE_AD_ACTIVITY_PREFIX = "com.google.android.gms.ads"
        private const val TAG = "AppOpenAds"
    }
}
