package com.example.compressimage.backgroundremoval

import com.example.compressimage.domain.model.BackgroundRemovalResult
import com.example.compressimage.domain.model.ImageSource
import com.example.compressimage.domain.repository.BackgroundRemovalRepository
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
            "Background removal needs a production segmentation model or configured online provider. No images are uploaded and no fake result is generated.",
        )
    }
}
