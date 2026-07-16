package com.example.compressimage.domain.model

enum class TargetSizeUnit {
    KB,
    MB,
}

enum class TargetSizePreset(val label: String, val bytes: Long?) {
    KB_100("100 KB", 100L * 1024L),
    KB_200("200 KB", 200L * 1024L),
    KB_500("500 KB", 500L * 1024L),
    MB_1("1 MB", 1024L * 1024L),
    CUSTOM("Custom", null),
}

data class TargetSize(
    val preset: TargetSizePreset = TargetSizePreset.KB_200,
    val customValue: String = "",
    val customUnit: TargetSizeUnit = TargetSizeUnit.KB,
) {
    fun bytesOrNull(): Long? {
        preset.bytes?.let { return it }
        val number = customValue.trim().toDoubleOrNull() ?: return null
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

enum class CompressionMode(
    val title: String,
    val description: String,
) {
    BEST_QUALITY(
        title = "Best Quality",
        description = "Prioritizes detail and avoids visible blur.",
    ),
    BALANCED(
        title = "Balanced",
        description = "Good quality with meaningful size reduction.",
    ),
    SMALLEST_SIZE(
        title = "Smallest Size",
        description = "Stronger compression while staying usable.",
    ),
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
