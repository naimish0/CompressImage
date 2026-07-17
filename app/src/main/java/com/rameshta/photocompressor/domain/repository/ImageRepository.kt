package com.rameshta.photocompressor.domain.repository

import com.rameshta.photocompressor.domain.model.BackgroundReplacementConfig
import com.rameshta.photocompressor.domain.model.CompressionConfig
import com.rameshta.photocompressor.domain.model.ImageInfo
import com.rameshta.photocompressor.domain.model.ProcessedImage
import com.rameshta.photocompressor.domain.model.SavedImage

interface ImageRepository {
    suspend fun loadImageInfo(uriString: String): Result<ImageInfo>

    suspend fun compressImage(
        image: ImageInfo,
        config: CompressionConfig,
        progress: (Float) -> Unit,
    ): Result<ProcessedImage>

    suspend fun replaceBackground(
        image: ProcessedImage,
        config: BackgroundReplacementConfig,
        progress: (Float) -> Unit,
    ): Result<ProcessedImage>

    suspend fun saveImage(
        image: ProcessedImage,
        requestedName: String? = null,
    ): Result<SavedImage>

    suspend fun saveAll(images: List<ProcessedImage>): List<Result<SavedImage>>

    suspend fun cleanupObsoleteTempFiles()
}
