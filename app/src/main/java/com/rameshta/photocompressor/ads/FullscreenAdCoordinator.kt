package com.rameshta.photocompressor.ads

import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FullscreenAdCoordinator @Inject constructor() {
    private val lock = Any()
    private val mutableIsShowing = MutableStateFlow(false)
    private var suppressNextAppOpenAd = false
    private var lastFullscreenAdFinishedAtMs = 0L

    val isShowing: StateFlow<Boolean> = mutableIsShowing.asStateFlow()

    fun tryAcquire(): Boolean = synchronized(lock) {
        if (mutableIsShowing.value) {
            false
        } else {
            mutableIsShowing.value = true
            true
        }
    }

    fun release(suppressNextAppOpen: Boolean = true) {
        synchronized(lock) {
            mutableIsShowing.value = false
            lastFullscreenAdFinishedAtMs = SystemClock.elapsedRealtime()
            if (suppressNextAppOpen) {
                suppressNextAppOpenAd = true
            }
        }
    }

    fun suppressNextAppOpen() {
        synchronized(lock) {
            suppressNextAppOpenAd = true
        }
    }

    fun consumeAppOpenSuppression(): Boolean = synchronized(lock) {
        val shouldSuppress = suppressNextAppOpenAd
        suppressNextAppOpenAd = false
        shouldSuppress
    }

    fun recentlyFinishedFullscreenAd(windowMs: Long): Boolean {
        val finishedAtMs = synchronized(lock) { lastFullscreenAdFinishedAtMs }
        return finishedAtMs > 0L && SystemClock.elapsedRealtime() - finishedAtMs < windowMs
    }
}
