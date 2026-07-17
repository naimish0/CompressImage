package com.rameshta.photocompressor.domain.model

enum class ImageFormat(
    val displayName: String,
    val extension: String,
    val mimeType: String,
    val supportsTransparency: Boolean,
) {
    JPEG("JPG", "jpg", "image/jpeg", false),
    PNG("PNG", "png", "image/png", true),
    WEBP("WEBP", "webp", "image/webp", true),
    UNKNOWN("Unknown", "img", "application/octet-stream", false);

    companion object {
        fun fromMimeType(mimeType: String?): ImageFormat {
            return when (mimeType?.lowercase()) {
                "image/jpeg", "image/jpg" -> JPEG
                "image/png" -> PNG
                "image/webp" -> WEBP
                else -> UNKNOWN
            }
        }
    }
}
