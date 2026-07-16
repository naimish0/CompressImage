package com.example.compressimage.util

import com.example.compressimage.domain.model.ImageFormat
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
        val base = originalName.substringBeforeLast('.', originalName)
            .ifBlank { "photo" }
            .replace(unsafeChars, "_")
            .trim('_', '.', '-')
            .ifBlank { "photo" }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(timestampMillis))
        return "${base}_${suffix}_$timestamp.${format.extension}"
    }

    fun ensureExtension(name: String, format: ImageFormat): String {
        val clean = name.trim().replace(unsafeChars, "_").ifBlank { "photo" }
        val extension = ".${format.extension}"
        return if (clean.endsWith(extension, ignoreCase = true)) clean else "$clean$extension"
    }
}
