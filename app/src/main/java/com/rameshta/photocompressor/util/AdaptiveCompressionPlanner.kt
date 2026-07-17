package com.rameshta.photocompressor.util

import com.rameshta.photocompressor.domain.model.CompressionMode
import com.rameshta.photocompressor.domain.model.Dimension
import kotlin.math.ceil
import kotlin.math.roundToInt

data class CompressionQualityPolicy(
    val defaultQuality: Int,
    val minAcceptableQuality: Int,
    val resizeFactor: Float,
    val maxResizeSteps: Int,
    val maxInitialLongSide: Int,
    val minOutputShortSide: Int,
)

data class EncodedCandidate(
    val bytes: ByteArray,
    val quality: Int,
    val targetReached: Boolean,
) {
    val sizeBytes: Int = bytes.size
}

object AdaptiveCompressionPlanner {
    const val MAX_QUALITY = 95
    const val TARGET_TOLERANCE_PERCENT = 5

    fun policyFor(mode: CompressionMode): CompressionQualityPolicy {
        return when (mode) {
            CompressionMode.BEST_QUALITY -> CompressionQualityPolicy(
                defaultQuality = 95,
                minAcceptableQuality = 82,
                resizeFactor = 0.94f,
                maxResizeSteps = 5,
                maxInitialLongSide = 5200,
                minOutputShortSide = 900,
            )
            CompressionMode.BALANCED -> CompressionQualityPolicy(
                defaultQuality = 92,
                minAcceptableQuality = 74,
                resizeFactor = 0.90f,
                maxResizeSteps = 8,
                maxInitialLongSide = 4600,
                minOutputShortSide = 640,
            )
            CompressionMode.SMALLEST_SIZE -> CompressionQualityPolicy(
                defaultQuality = 88,
                minAcceptableQuality = 62,
                resizeFactor = 0.88f,
                maxResizeSteps = 11,
                maxInitialLongSide = 3800,
                minOutputShortSide = 420,
            )
        }
    }

    fun toleratedTargetBytes(targetBytes: Long): Long {
        return ceil(targetBytes * (100 + TARGET_TOLERANCE_PERCENT) / 100.0).toLong()
    }

    fun targetReached(sizeBytes: Long, targetBytes: Long): Boolean {
        return sizeBytes <= toleratedTargetBytes(targetBytes)
    }

    fun fitWithin(dimension: Dimension, maxLongSide: Int): Dimension {
        val longSide = maxOf(dimension.width, dimension.height)
        if (longSide <= maxLongSide) return dimension
        val ratio = maxLongSide.toFloat() / longSide.toFloat()
        return Dimension(
            width = (dimension.width * ratio).roundToInt().coerceAtLeast(1),
            height = (dimension.height * ratio).roundToInt().coerceAtLeast(1),
        )
    }

    fun nextReducedDimension(
        dimension: Dimension,
        resizeFactor: Float,
        minShortSide: Int,
    ): Dimension? {
        val shortSide = minOf(dimension.width, dimension.height)
        if (shortSide <= minShortSide) return null
        val nextWidth = (dimension.width * resizeFactor).roundToInt().coerceAtLeast(minShortSide)
        val nextHeight = (dimension.height * resizeFactor).roundToInt().coerceAtLeast(minShortSide)
        if (nextWidth >= dimension.width && nextHeight >= dimension.height) return null
        return Dimension(nextWidth, nextHeight)
    }

    fun searchHighestQuality(
        targetBytes: Long,
        minQuality: Int,
        maxQuality: Int = MAX_QUALITY,
        encoder: (Int) -> ByteArray,
    ): EncodedCandidate {
        val targetLimit = toleratedTargetBytes(targetBytes)
        val safeMinQuality = minQuality.coerceIn(1, maxQuality)
        var low = safeMinQuality
        var high = maxQuality
        var bestUnder: EncodedCandidate? = null
        var minQualityCandidate: EncodedCandidate? = null

        while (low <= high) {
            val quality = (low + high) / 2
            val bytes = encoder(quality)
            val candidate = EncodedCandidate(
                bytes = bytes,
                quality = quality,
                targetReached = bytes.size.toLong() <= targetLimit,
            )
            if (quality == safeMinQuality) {
                minQualityCandidate = candidate
            }
            if (candidate.targetReached) {
                bestUnder = candidate
                low = quality + 1
            } else {
                high = quality - 1
            }
        }

        return bestUnder ?: minQualityCandidate ?: EncodedCandidate(
            bytes = encoder(safeMinQuality),
            quality = safeMinQuality,
            targetReached = false,
        )
    }
}
