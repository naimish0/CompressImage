package com.example.compressimage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.compressimage.ads.AdsInitializer
import com.example.compressimage.ads.BannerAdController
import com.example.compressimage.ads.ConsentManager
import com.example.compressimage.ads.InterstitialAdManager
import com.example.compressimage.data.storage.ImageShareController
import com.example.compressimage.ui.PhotoCompressorViewModel
import com.example.compressimage.ui.navigation.PhotoCompressorApp
import com.example.compressimage.ui.theme.CompressImageTheme
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
