package com.rameshta.photocompressor

import android.app.Application
import com.rameshta.photocompressor.ads.AppOpenAdManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class PhotoCompressorApplication : Application() {
    @Inject lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()
        appOpenAdManager.register(this)
    }
}
