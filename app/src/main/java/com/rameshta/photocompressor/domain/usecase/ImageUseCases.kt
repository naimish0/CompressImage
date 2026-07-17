package com.rameshta.photocompressor.domain.usecase

import com.rameshta.photocompressor.domain.model.BackgroundRemovalResult
import com.rameshta.photocompressor.domain.model.BackgroundReplacementConfig
import com.rameshta.photocompressor.domain.model.CompressionConfig
import com.rameshta.photocompressor.domain.model.ImageInfo
import com.rameshta.photocompressor.domain.model.ImageSource
import com.rameshta.photocompressor.domain.model.ProcessedImage
import com.rameshta.photocompressor.domain.model.SavedImage
import com.rameshta.photocompressor.domain.repository.BackgroundRemovalRepository
import com.rameshta.photocompressor.domain.repository.ImageRepository
import javax.inject.Inject

class LoadImageInfoUseCase @Inject constructor(
    private val repository: ImageRepository,
) {
    suspend operator fun invoke(uriString: String): Result<ImageInfo> {
        return repository.loadImageInfo(uriString)
    }
}

class CompressImageUseCase @Inject constructor(
    private val repository: ImageRepository,
) {
    suspend operator fun invoke(
        image: ImageInfo,
        config: CompressionConfig,
        progress: (Float) -> Unit,
    ): Result<ProcessedImage> {
        return repository.compressImage(image, config, progress)
    }
}

class SaveImagesUseCase @Inject constructor(
    private val repository: ImageRepository,
) {
    suspend fun saveOne(image: ProcessedImage, requestedName: String? = null): Result<SavedImage> {
        return repository.saveImage(image, requestedName)
    }

    suspend fun saveAll(images: List<ProcessedImage>): List<Result<SavedImage>> {
        return repository.saveAll(images)
    }
}

class RemoveBackgroundUseCase @Inject constructor(
    private val repository: BackgroundRemovalRepository,
) {
    suspend operator fun invoke(
        input: ImageSource,
        progress: (Float) -> Unit,
    ): BackgroundRemovalResult {
        return repository.removeBackground(input, progress)
    }
}

class ReplaceBackgroundUseCase @Inject constructor(
    private val repository: ImageRepository,
) {
    suspend operator fun invoke(
        image: ProcessedImage,
        config: BackgroundReplacementConfig,
        progress: (Float) -> Unit,
    ): Result<ProcessedImage> {
        return repository.replaceBackground(image, config, progress)
    }
}
