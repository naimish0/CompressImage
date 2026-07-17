package com.rameshta.photocompressor.ads

import android.app.Activity
import kotlinx.coroutines.flow.StateFlow

interface InterstitialAdManager {
    val isFullScreenAdShowing: StateFlow<Boolean>

    fun preload(placement: InterstitialPlacement? = null)

    fun canShow(placement: InterstitialPlacement): Boolean

    fun show(
        activity: Activity,
        placement: InterstitialPlacement,
        onFinished: () -> Unit,
    )
}
