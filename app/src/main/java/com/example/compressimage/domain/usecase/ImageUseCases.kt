package com.example.compressimage.domain.usecase

import com.example.compressimage.domain.model.BackgroundRemovalResult
import com.example.compressimage.domain.model.BackgroundReplacementConfig
import com.example.compressimage.domain.model.CompressionConfig
import com.example.compressimage.domain.model.ImageInfo
import com.example.compressimage.domain.model.ImageSource
import com.example.compressimage.domain.model.ProcessedImage
import com.example.compressimage.domain.model.SavedImage
import com.example.compressimage.domain.repository.BackgroundRemovalRepository
import com.example.compressimage.domain.repository.ImageRepository
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
