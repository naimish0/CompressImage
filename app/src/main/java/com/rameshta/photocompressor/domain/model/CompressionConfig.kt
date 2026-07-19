package com.rameshta.photocompressor.domain.model

import com.rameshta.photocompressor.util.toLocalizedDecimalOrNull

enum class TargetSizeUnit {
    KB,
    MB,
}

enum class TargetSizePreset(val bytes: Long?) {
    KB_100(100L * 1024L),
    KB_200(200L * 1024L),
    KB_500(500L * 1024L),
    MB_1(1024L * 1024L),
    CUSTOM(null),
}

data class TargetSize(
    val preset: TargetSizePreset = TargetSizePreset.KB_200,
    val customValue: String = "",
    val customUnit: TargetSizeUnit = TargetSizeUnit.KB,
) {
    fun bytesOrNull(): Long? {
        preset.bytes?.let { return it }
        val number = customValue.toLocalizedDecimalOrNull() ?: return null
        val multiplier = when (customUnit) {
            TargetSizeUnit.KB -> 1024.0
            TargetSizeUnit.MB -> 1024.0 * 1024.0
        }
        return (number * multiplier).toLong()
    }
}

enum class ResizeMode {
    ORIGINAL,
    PERCENT_25,
    PERCENT_50,
    PERCENT_75,
    CUSTOM,
}

enum class CompressionMode {
    BEST_QUALITY,
    BALANCED,
    SMALLEST_SIZE,
}

data class ResizeConfig(
    val mode: ResizeMode = ResizeMode.ORIGINAL,
    val customWidth: String = "",
    val customHeight: String = "",
    val maintainAspectRatio: Boolean = true,
    val allowUpscale: Boolean = false,
)

data class CompressionConfig(
    val targetSize: TargetSize = TargetSize(),
    val compressionMode: CompressionMode = CompressionMode.BALANCED,
    val resize: ResizeConfig = ResizeConfig(),
    val outputFormat: ImageFormat = ImageFormat.JPEG,
    val jpegBackgroundColor: Int = 0xFFFFFFFF.toInt(),
    val stripLocationMetadata: Boolean = true,
)

data class Dimension(
    val width: Int,
    val height: Int,
)
