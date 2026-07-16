package com.example.compressimage.backgroundremoval

import android.graphics.Bitmap

interface BackgroundRemovalEngine {
    suspend fun removeBackground(
        bitmap: Bitmap,
        config: BackgroundRemovalConfig,
    ): BackgroundMask
}

data class BackgroundMask(
    val alpha: ByteArray,
    val width: Int,
    val height: Int,
    val metadata: ModelMetadata,
)
