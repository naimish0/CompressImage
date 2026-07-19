package com.rameshta.photocompressor.util

import com.rameshta.photocompressor.domain.model.TargetSize
import com.rameshta.photocompressor.domain.model.TargetSizeUnit

data class ValidationResult(
    val isValid: Boolean,
    val message: ValidationMessage? = null,
)

enum class ValidationMessage {
    ENTER_TARGET_SIZE,
    TARGET_SIZE_GREATER_THAN_ZERO,
    TARGET_SIZE_TOO_SMALL,
    TARGET_SIZE_TOO_LARGE,
    ENTER_VALID_NUMBER,
    USE_MB_FOR_LARGE_TARGETS,
    ORIGINAL_DIMENSIONS_UNAVAILABLE,
    ENTER_VALID_WIDTH_AND_HEIGHT,
    OUTPUT_DIMENSIONS_TOO_SMALL,
    OUTPUT_DIMENSIONS_TOO_LARGE,
    UPSCALING_IS_OFF,
}

object TargetSizeValidator {
    private const val MIN_BYTES = 10L * 1024L
    private const val MAX_BYTES = 200L * 1024L * 1024L

    fun validate(targetSize: TargetSize): ValidationResult {
        val bytes = targetSize.bytesOrNull()
            ?: return ValidationResult(false, ValidationMessage.ENTER_TARGET_SIZE)
        if (bytes <= 0L) return ValidationResult(false, ValidationMessage.TARGET_SIZE_GREATER_THAN_ZERO)
        if (bytes < MIN_BYTES) return ValidationResult(false, ValidationMessage.TARGET_SIZE_TOO_SMALL)
        if (bytes > MAX_BYTES) return ValidationResult(false, ValidationMessage.TARGET_SIZE_TOO_LARGE)
        val customValue = targetSize.customValue.trim()
        if (targetSize.preset.bytes == null) {
            val number = customValue.toLocalizedDecimalOrNull()
                ?: return ValidationResult(false, ValidationMessage.ENTER_VALID_NUMBER)
            if (number <= 0.0) return ValidationResult(false, ValidationMessage.TARGET_SIZE_GREATER_THAN_ZERO)
            if (targetSize.customUnit == TargetSizeUnit.KB && number > 200_000.0) {
                return ValidationResult(false, ValidationMessage.USE_MB_FOR_LARGE_TARGETS)
            }
        }
        return ValidationResult(true)
    }
}

internal fun String.toLocalizedDecimalOrNull(): Double? {
    val value = trim()
    if (value.isEmpty()) return null

    var decimalSeparatorSeen = false
    val normalized = buildString(value.length) {
        value.forEachIndexed { index, character ->
            val digit = Character.digit(character, 10)
            when {
                digit >= 0 -> append(digit)
                character == '.' || character == ',' || character == '\u066B' -> {
                    if (decimalSeparatorSeen) return null
                    decimalSeparatorSeen = true
                    append('.')
                }
                (character == '+' || character == '-') && index == 0 -> append(character)
                else -> return null
            }
        }
    }
    return normalized.toDoubleOrNull()
}

internal fun String.toLocalizedIntOrNull(): Int? {
    val value = trim()
    if (value.isEmpty()) return null

    val normalized = buildString(value.length) {
        value.forEachIndexed { index, character ->
            val digit = Character.digit(character, 10)
            when {
                digit >= 0 -> append(digit)
                (character == '+' || character == '-') && index == 0 -> append(character)
                else -> return null
            }
        }
    }
    return normalized.toIntOrNull()
}
