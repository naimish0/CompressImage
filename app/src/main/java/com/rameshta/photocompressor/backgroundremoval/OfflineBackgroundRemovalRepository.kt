package com.rameshta.photocompressor.backgroundremoval

import com.rameshta.photocompressor.domain.model.BackgroundRemovalResult
import com.rameshta.photocompressor.domain.model.BackgroundFailure
import com.rameshta.photocompressor.domain.model.ImageSource
import com.rameshta.photocompressor.domain.repository.BackgroundRemovalRepository
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineBackgroundRemovalRepository @Inject constructor() : BackgroundRemovalRepository {
    override suspend fun removeBackground(
        input: ImageSource,
        progress: (Float) -> Unit,
    ): BackgroundRemovalResult {
        progress(0.15f)
        delay(120)
        progress(1f)
        return BackgroundRemovalResult.Unavailable(
            BackgroundFailure.FEATURE_UNAVAILABLE,
        )
    }
}
