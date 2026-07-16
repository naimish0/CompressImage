package com.example.compressimage.ads

import android.app.Activity
import android.content.Context
import com.example.compressimage.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterstitialAdController @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    private var interstitialAd: InterstitialAd? = null
    private var isLoading = false
    private var isShowing = false

    fun preload() {
        if (!BuildConfig.ADS_ENABLED || isLoading || interstitialAd != null) return
        isLoading = true
        InterstitialAd.load(
            context,
            BuildConfig.ADMOB_INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    isLoading = false
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    isLoading = false
                    interstitialAd = null
                }
            },
        )
    }

    fun showIfAvailable(activity: Activity, onFinished: () -> Unit) {
        if (!BuildConfig.ADS_ENABLED || isShowing) {
            onFinished()
            return
        }
        val ad = interstitialAd
        if (ad == null) {
            preload()
            onFinished()
            return
        }
        interstitialAd = null
        isShowing = true
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                finish()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                finish()
            }

            private fun finish() {
                isShowing = false
                preload()
                onFinished()
            }
        }
        ad.show(activity)
    }
}
