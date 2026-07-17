package com.rameshta.photocompressor.util

import java.util.Locale

object FileSizeFormatter {
    fun format(bytes: Long): String {
        if (bytes < 0L) return "Unknown"
        if (bytes < 1024L) return "$bytes B"
        val kb = bytes / 1024.0
        if (kb < 1024.0) return formatUnit(kb, "KB")
        val mb = kb / 1024.0
        if (mb < 1024.0) return formatUnit(mb, "MB")
        return formatUnit(mb / 1024.0, "GB")
    }

    private fun formatUnit(value: Double, unit: String): String {
        val pattern = if (value >= 10.0) "%.0f %s" else "%.1f %s"
        return String.format(Locale.US, pattern, value, unit)
    }
}
