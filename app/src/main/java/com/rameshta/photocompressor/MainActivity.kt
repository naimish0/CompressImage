package com.rameshta.photocompressor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.rameshta.photocompressor.ads.AdsInitializer
import com.rameshta.photocompressor.ads.BannerAdController
import com.rameshta.photocompressor.ads.ConsentManager
import com.rameshta.photocompressor.ads.InterstitialAdManager
import com.rameshta.photocompressor.data.storage.ImageShareController
import com.rameshta.photocompressor.ui.PhotoCompressorViewModel
import com.rameshta.photocompressor.ui.navigation.PhotoCompressorApp
import com.rameshta.photocompressor.ui.theme.CompressImageTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var consentManager: ConsentManager
    @Inject lateinit var adsInitializer: AdsInitializer
    @Inject lateinit var bannerAdController: BannerAdController
    @Inject lateinit var interstitialAdManager: InterstitialAdManager
    @Inject lateinit var imageShareController: ImageShareController

    private val viewModel: PhotoCompressorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CompressImageTheme {
                PhotoCompressorApp(
                    viewModel = viewModel,
                    consentManager = consentManager,
                    adsInitializer = adsInitializer,
                    bannerAdController = bannerAdController,
                    interstitialAdManager = interstitialAdManager,
                    imageShareController = imageShareController,
                )
            }
        }
    }
}
