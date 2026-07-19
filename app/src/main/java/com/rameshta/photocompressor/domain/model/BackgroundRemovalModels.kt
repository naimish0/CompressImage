package com.rameshta.photocompressor.domain.model

sealed interface BackgroundRemovalResult {
    data class Success(val image: ProcessedImage) : BackgroundRemovalResult
    data class Unavailable(val reason: BackgroundFailure) : BackgroundRemovalResult
    data class Failure(val reason: BackgroundFailure) : BackgroundRemovalResult
}

data class BackgroundReplacementConfig(
    val colorArgb: Int?,
    val outputFormat: ImageFormat = ImageFormat.PNG,
) {
    companion object {
        val Transparent = BackgroundReplacementConfig(colorArgb = null, outputFormat = ImageFormat.PNG)
    }
}
