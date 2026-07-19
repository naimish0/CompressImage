package com.rameshta.photocompressor.ui

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.rameshta.photocompressor.R
import com.rameshta.photocompressor.domain.model.BackgroundFailure
import com.rameshta.photocompressor.domain.model.ImageErrorCode
import com.rameshta.photocompressor.domain.model.ImageOperationException
import com.rameshta.photocompressor.domain.model.ProcessingNotice
import com.rameshta.photocompressor.util.ValidationMessage

sealed interface UiText {
    data class Resource(
        @param:StringRes val resId: Int,
        val formatArgs: List<Any> = emptyList(),
    ) : UiText

    data class Plural(
        @param:PluralsRes val resId: Int,
        val quantity: Int,
        val formatArgs: List<Any> = listOf(quantity),
    ) : UiText

    data class Joined(
        val values: List<UiText>,
        val separator: String = "\n",
    ) : UiText
}

fun uiText(@StringRes resId: Int, vararg formatArgs: Any): UiText =
    UiText.Resource(resId, formatArgs.toList())

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Resource -> stringResource(resId, *formatArgs.toTypedArray())
    is UiText.Plural -> pluralStringResource(resId, quantity, *formatArgs.toTypedArray())
    is UiText.Joined -> values
        .map { value -> value.asString() }
        .distinct()
        .joinToString(separator)
}

fun Throwable.toUiText(@StringRes fallback: Int): UiText {
    val operationError = this as? ImageOperationException
    return operationError?.code?.toUiText(operationError.formatArgs) ?: uiText(fallback)
}

fun ImageErrorCode.toUiText(formatArgs: List<Any> = emptyList()): UiText {
    val resId = when (this) {
        ImageErrorCode.FILE_NOT_READABLE -> R.string.error_file_not_readable
        ImageErrorCode.UNSUPPORTED_FORMAT -> R.string.error_unsupported_image_format
        ImageErrorCode.INVALID_OUTPUT_SIZE -> R.string.error_invalid_output_size
        ImageErrorCode.IMAGE_TOO_LARGE -> R.string.error_image_too_large
        ImageErrorCode.PROCESSED_IMAGE_UNAVAILABLE -> R.string.error_processed_image_unavailable
        ImageErrorCode.CREATE_PICTURES_FOLDER_FAILED -> R.string.error_create_pictures_folder
        ImageErrorCode.CREATE_MEDIA_ENTRY_FAILED -> R.string.error_create_media_entry
        ImageErrorCode.OPEN_SAVE_DESTINATION_FAILED -> R.string.error_open_save_destination
        ImageErrorCode.IMAGE_DECODE_FAILED -> R.string.error_image_decode_failed
        ImageErrorCode.DIMENSIONS_TOO_LARGE -> R.string.error_dimensions_too_large
        ImageErrorCode.IMAGE_READ_FAILED -> R.string.error_image_read_failed
        ImageErrorCode.NO_READABLE_IMAGE_STREAM -> R.string.error_no_readable_image_stream
        ImageErrorCode.PHOTO_ACCESS_DENIED -> R.string.error_photo_access_denied
        ImageErrorCode.CLOUD_IMAGE_UNAVAILABLE -> R.string.error_cloud_image_unavailable
        ImageErrorCode.IMAGE_ENCODE_FAILED -> R.string.error_image_encode_failed
        ImageErrorCode.COMPRESSED_IMAGE_VALIDATION_FAILED -> R.string.error_compressed_image_validation_failed
        ImageErrorCode.STORAGE_PERMISSION_REQUIRED -> R.string.error_allow_storage_access
        ImageErrorCode.SHARED_STORAGE_UNAVAILABLE -> R.string.error_shared_storage_unavailable
    }
    return UiText.Resource(resId, formatArgs)
}

fun BackgroundFailure.toUiText(): UiText = uiText(
    when (this) {
        BackgroundFailure.MODEL_MISSING -> R.string.error_background_model_missing
        BackgroundFailure.MODEL_LOAD_FAILED -> R.string.error_background_model_load_failed
        BackgroundFailure.DECODE_FAILED -> R.string.error_background_decode_failed
        BackgroundFailure.OUT_OF_MEMORY -> R.string.error_background_out_of_memory
        BackgroundFailure.INFERENCE_FAILED -> R.string.error_background_inference_failed
        BackgroundFailure.EXPORT_FAILED -> R.string.error_background_export_failed
        BackgroundFailure.MODEL_INCOMPATIBLE -> R.string.error_background_model_incompatible
        BackgroundFailure.TRY_SMALLER_IMAGE -> R.string.error_background_try_smaller
        BackgroundFailure.FEATURE_UNAVAILABLE -> R.string.error_background_feature_unavailable
        BackgroundFailure.GENERIC -> R.string.error_background_generic
    },
)

fun ValidationMessage.toUiText(): UiText = uiText(
    when (this) {
        ValidationMessage.ENTER_TARGET_SIZE -> R.string.validation_enter_target_size
        ValidationMessage.TARGET_SIZE_GREATER_THAN_ZERO -> R.string.validation_target_size_greater_than_zero
        ValidationMessage.TARGET_SIZE_TOO_SMALL -> R.string.validation_target_size_too_small
        ValidationMessage.TARGET_SIZE_TOO_LARGE -> R.string.validation_target_size_too_large
        ValidationMessage.ENTER_VALID_NUMBER -> R.string.validation_enter_valid_number
        ValidationMessage.USE_MB_FOR_LARGE_TARGETS -> R.string.validation_use_mb_for_large_targets
        ValidationMessage.ORIGINAL_DIMENSIONS_UNAVAILABLE -> R.string.validation_original_dimensions_unavailable
        ValidationMessage.ENTER_VALID_WIDTH_AND_HEIGHT -> R.string.validation_enter_valid_width_and_height
        ValidationMessage.OUTPUT_DIMENSIONS_TOO_SMALL -> R.string.validation_output_dimensions_too_small
        ValidationMessage.OUTPUT_DIMENSIONS_TOO_LARGE -> R.string.validation_output_dimensions_too_large
        ValidationMessage.UPSCALING_IS_OFF -> R.string.validation_upscaling_is_off
    },
)

@Composable
fun ValidationMessage.asString(): String = toUiText().asString()

fun ProcessingNotice.toUiText(): UiText = uiText(
    when (this) {
        ProcessingNotice.PNG_LOSSLESS_ORIGINAL_PRESERVED -> R.string.warning_png_lossless_original_preserved
        ProcessingNotice.PNG_LOSSLESS_SAFE_RESIZE -> R.string.warning_png_lossless_safe_resize
        ProcessingNotice.PNG_TARGET_UNREACHED_ORIGINAL -> R.string.warning_png_target_unreached_original
        ProcessingNotice.PNG_TARGET_UNREACHED_BEST_QUALITY -> R.string.warning_png_target_unreached_best_quality
        ProcessingNotice.TARGET_UNREACHED_ORIGINAL -> R.string.warning_target_unreached_original
        ProcessingNotice.TARGET_UNREACHED_BEST_QUALITY -> R.string.warning_target_unreached_best_quality
        ProcessingNotice.TARGET_REACHED_REDUCED_RESOLUTION -> R.string.warning_target_reached_reduced_resolution
        ProcessingNotice.BACKGROUND_SAFE_RESOLUTION -> R.string.warning_background_safe_resolution
    },
)

@Composable
fun ProcessingNotice.asString(): String = toUiText().asString()
