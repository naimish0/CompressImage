package com.example.compressimage.domain.model

data class ImageSource(
    val id: String,
    val uriString: String,
)

data class ImageInfo(
    val id: String,
    val uriString: String,
    val displayName: String,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val format: ImageFormat,
    val mimeType: String,
    val hasAlpha: Boolean,
) {
    val resolutionLabel: String = "$width x $height"
}

data class ProcessedImage(
    val id: String,
    val original: ImageInfo,
    val filePath: String,
    val displayName: String,
    val sizeBytes: Long,
    val width: Int,
    val height: Int,
    val format: ImageFormat,
    val mimeType: String,
    val requestedTargetBytes: Long? = null,
    val targetLimitBytes: Long? = null,
    val targetReached: Boolean? = null,
    val outputQuality: Int? = null,
    val compressionMode: CompressionMode? = null,
    val operationType: HistoryOperationType = HistoryOperationType.COMPRESSED,
    val savedUriString: String? = null,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val warning: String? = null,
) {
    val resolutionLabel: String = "$width x $height"
}

enum class HistoryOperationType {
    COMPRESSED,
    BACKGROUND_REMOVED,
    RESIZED,
    FORMAT_CONVERTED,
    ENHANCED,
    COLLAGE,
}

data class SavedImage(
    val uriString: String,
    val displayName: String,
)

data class CompressionStats(
    val originalSizeBytes: Long,
    val processedSizeBytes: Long,
    val savedBytes: Long,
    val percentageSaved: Double,
    val compressionRatio: Double,
)

data class ProcessingSummary(
    val total: Int,
    val successful: Int,
    val failed: Int,
    val cancelled: Boolean,
)
