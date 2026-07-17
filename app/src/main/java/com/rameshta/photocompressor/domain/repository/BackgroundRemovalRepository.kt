package com.rameshta.photocompressor.domain.repository

import com.rameshta.photocompressor.domain.model.BackgroundRemovalResult
import com.rameshta.photocompressor.domain.model.ImageSource

interface BackgroundRemovalRepository {
    suspend fun removeBackground(
        input: ImageSource,
        progress: (Float) -> Unit,
    ): BackgroundRemovalResult
}
