package com.example.compressimage.domain.repository

import com.example.compressimage.domain.model.BackgroundRemovalResult
import com.example.compressimage.domain.model.ImageSource

interface BackgroundRemovalRepository {
    suspend fun removeBackground(
        input: ImageSource,
        progress: (Float) -> Unit,
    ): BackgroundRemovalResult
}
