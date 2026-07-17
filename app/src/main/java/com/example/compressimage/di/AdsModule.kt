package com.example.compressimage.di

import com.example.compressimage.ads.BannerAdController
import com.example.compressimage.ads.ConsentManager
import com.example.compressimage.ads.GoogleBannerAdController
import com.example.compressimage.ads.GoogleConsentManager
import com.example.compressimage.ads.GoogleInterstitialAdManager
import com.example.compressimage.ads.InterstitialAdManager
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
