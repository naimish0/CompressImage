package com.rameshta.photocompressor.domain.model

import java.io.IOException

/** Stable error categories that can be mapped to localized UI resources. */
enum class ImageErrorCode {
    FILE_NOT_READABLE,
    UNSUPPORTED_FORMAT,
    INVALID_OUTPUT_SIZE,
    IMAGE_TOO_LARGE,
    PROCESSED_IMAGE_UNAVAILABLE,
    CREATE_PICTURES_FOLDER_FAILED,
    CREATE_MEDIA_ENTRY_FAILED,
    OPEN_SAVE_DESTINATION_FAILED,
    IMAGE_DECODE_FAILED,
    DIMENSIONS_TOO_LARGE,
    IMAGE_READ_FAILED,
    NO_READABLE_IMAGE_STREAM,
    PHOTO_ACCESS_DENIED,
    CLOUD_IMAGE_UNAVAILABLE,
    IMAGE_ENCODE_FAILED,
    COMPRESSED_IMAGE_VALIDATION_FAILED,
    STORAGE_PERMISSION_REQUIRED,
    SHARED_STORAGE_UNAVAILABLE,
}

class ImageOperationException(
    val code: ImageErrorCode,
    val formatArgs: List<Any> = emptyList(),
    cause: Throwable? = null,
) : IOException(code.name, cause)

enum class BackgroundFailure {
    MODEL_MISSING,
    MODEL_LOAD_FAILED,
    DECODE_FAILED,
    OUT_OF_MEMORY,
    INFERENCE_FAILED,
    EXPORT_FAILED,
    MODEL_INCOMPATIBLE,
    TRY_SMALLER_IMAGE,
    FEATURE_UNAVAILABLE,
    GENERIC,
}

/** Persist this enum name, never translated text. */
enum class ProcessingNotice {
    PNG_LOSSLESS_ORIGINAL_PRESERVED,
    PNG_LOSSLESS_SAFE_RESIZE,
    PNG_TARGET_UNREACHED_ORIGINAL,
    PNG_TARGET_UNREACHED_BEST_QUALITY,
    TARGET_UNREACHED_ORIGINAL,
    TARGET_UNREACHED_BEST_QUALITY,
    TARGET_REACHED_REDUCED_RESOLUTION,
    BACKGROUND_SAFE_RESOLUTION,
}
