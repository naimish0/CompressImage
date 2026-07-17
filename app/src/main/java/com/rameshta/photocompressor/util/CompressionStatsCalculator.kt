package com.rameshta.photocompressor.util

import com.rameshta.photocompressor.domain.model.CompressionStats

object CompressionStatsCalculator {
    fun calculate(originalSizeBytes: Long, processedSizeBytes: Long): CompressionStats {
        val saved = originalSizeBytes - processedSizeBytes
        val percent = if (originalSizeBytes > 0L) {
            (saved.toDouble() / originalSizeBytes.toDouble()) * 100.0
        } else {
            0.0
        }
        val ratio = if (processedSizeBytes > 0L) {
            originalSizeBytes.toDouble() / processedSizeBytes.toDouble()
        } else {
            0.0
        }
        return CompressionStats(
            originalSizeBytes = originalSizeBytes,
            processedSizeBytes = processedSizeBytes,
            savedBytes = saved,
            percentageSaved = percent,
            compressionRatio = ratio,
        )
    }
}
