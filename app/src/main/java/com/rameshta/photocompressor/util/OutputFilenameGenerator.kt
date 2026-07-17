package com.rameshta.photocompressor.util

import com.rameshta.photocompressor.domain.model.ImageFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object OutputFilenameGenerator {
    private val unsafeChars = Regex("[^A-Za-z0-9._-]+")

    fun generate(
        originalName: String,
        format: ImageFormat,
        suffix: String = "compressed",
        timestampMillis: Long = System.currentTimeMillis(),
    ): String {
        val base = sanitizeName(originalName.substringBeforeLast('.', originalName))
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(timestampMillis))
        return "${base}_${suffix}_$timestamp.${format.extension}"
    }

    fun ensureExtension(name: String, format: ImageFormat): String {
        val clean = sanitizeName(name)
        val extension = ".${format.extension}"
        return if (clean.endsWith(extension, ignoreCase = true)) clean else "$clean$extension"
    }

    private fun sanitizeName(name: String): String {
        return name.trim()
            .replace(unsafeChars, "_")
            .trim('_', '.', '-')
            .take(MAX_FILENAME_BASE_LENGTH)
            .ifBlank { "photo" }
    }

    private const val MAX_FILENAME_BASE_LENGTH = 80
}
