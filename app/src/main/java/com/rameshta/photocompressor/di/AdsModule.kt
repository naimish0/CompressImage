package com.rameshta.photocompressor.di

import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.ads.ConsentManager
import com.rameshta.photocompressor.ads.GoogleBannerAdController
import com.rameshta.photocompressor.ads.GoogleConsentManager
import com.rameshta.photocompressor.ads.GoogleInterstitialAdManager
import com.rameshta.photocompressor.ads.InterstitialAdManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AdsModule {
    @Binds
    @Singleton
    abstract fun bindConsentManager(manager: GoogleConsentManager): ConsentManager

    @Binds
    @Singleton
    abstract fun bindBannerAdController(controller: GoogleBannerAdController): BannerAdController

    @Binds
    @Singleton
    abstract fun bindInterstitialAdManager(manager: GoogleInterstitialAdManager): InterstitialAdManager
}
