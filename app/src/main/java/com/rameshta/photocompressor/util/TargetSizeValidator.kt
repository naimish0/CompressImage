package com.rameshta.photocompressor.util

import com.rameshta.photocompressor.domain.model.TargetSize
import com.rameshta.photocompressor.domain.model.TargetSizeUnit

data class ValidationResult(
    val isValid: Boolean,
    val message: String? = null,
)

object TargetSizeValidator {
    private const val MIN_BYTES = 10L * 1024L
    private const val MAX_BYTES = 200L * 1024L * 1024L

    fun validate(targetSize: TargetSize): ValidationResult {
        val bytes = targetSize.bytesOrNull()
            ?: return ValidationResult(false, "Enter a target size.")
        if (bytes <= 0L) return ValidationResult(false, "Target size must be greater than zero.")
        if (bytes < MIN_BYTES) return ValidationResult(false, "Target size is too small for reliable image output.")
        if (bytes > MAX_BYTES) return ValidationResult(false, "Target size is larger than this app supports.")
        val customValue = targetSize.customValue.trim()
        if (targetSize.preset.bytes == null) {
            val number = customValue.toDoubleOrNull()
                ?: return ValidationResult(false, "Enter a valid number.")
            if (number <= 0.0) return ValidationResult(false, "Target size must be greater than zero.")
            if (targetSize.customUnit == TargetSizeUnit.KB && number > 200_000.0) {
                return ValidationResult(false, "Use MB for very large targets.")
            }
        }
        return ValidationResult(true)
    }
}
