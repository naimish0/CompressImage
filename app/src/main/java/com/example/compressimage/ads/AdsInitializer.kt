package com.example.compressimage.ads

import android.content.Context
import android.util.Log
import com.example.compressimage.BuildConfig
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdsInitializer @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val configuration: AdsConfiguration,
) {
    private val lock = Any()
    private val _state = MutableStateFlow(AdsInitializationState())
    val state: StateFlow<AdsInitializationState> = _state.asStateFlow()

    fun initializeIfAllowed(canRequestAds: Boolean) {
        if (!configuration.adsEnabled || !canRequestAds) return
        synchronized(lock) {
            val current = _state.value
            if (current.initialized || current.initializing) return
            _state.value = current.copy(initializing = true)
        }
        MobileAds.setRequestConfiguration(RequestConfiguration.Builder().build())
        MobileAds.initialize(context) {
            logDebug("Google Mobile Ads initialized.")
            _state.update { it.copy(initialized = true, initializing = false) }
        }
    }

    private fun logDebug(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    private companion object {
        const val TAG = "AdsInitializer"
    }
}
