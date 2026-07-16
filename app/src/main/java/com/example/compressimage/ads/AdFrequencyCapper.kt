package com.example.compressimage.ads

import javax.inject.Inject

class AdFrequencyCapper @Inject constructor() {
    private val completedOperationsPerInterstitial: Int = 3
    private var completedOperations = 0

    fun recordCompletedOperation(shouldCount: Boolean = true): Boolean {
        if (!shouldCount || completedOperationsPerInterstitial <= 0) return false
        completedOperations += 1
        return completedOperations % completedOperationsPerInterstitial == 0
    }
}
