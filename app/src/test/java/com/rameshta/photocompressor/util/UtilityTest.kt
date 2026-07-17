package com.rameshta.photocompressor.util

import com.rameshta.photocompressor.ads.AdPlacementPolicy
import com.rameshta.photocompressor.ads.BannerPlacement
import com.rameshta.photocompressor.ads.InterstitialPlacement
import com.rameshta.photocompressor.domain.model.CompressionMode
import com.rameshta.photocompressor.domain.model.Dimension
import com.rameshta.photocompressor.domain.model.ImageFormat
import com.rameshta.photocompressor.domain.model.ImageInfo
import com.rameshta.photocompressor.domain.model.ProcessedImage
import com.rameshta.photocompressor.domain.model.ResizeConfig
import com.rameshta.photocompressor.domain.model.ResizeMode
import com.rameshta.photocompressor.domain.model.TargetSize
import com.rameshta.photocompressor.domain.model.TargetSizePreset
import com.rameshta.photocompressor.ui.history.HistoryListItem
import com.rameshta.photocompressor.ui.history.historyListItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class UtilityTest {
    @Test
    fun fileSizeFormattingUsesReadableUnits() {
        assertEquals("512 B", FileSizeFormatter.format(512))
        assertEquals("2.0 KB", FileSizeFormatter.format(2048))
        assertEquals("1.5 MB", FileSizeFormatter.format(1_572_864))
    }

    @Test
    fun compressionStatsHandleSavingsAndLargerOutput() {
        val saved = CompressionStatsCalculator.calculate(1_000, 250)
        assertEquals(750, saved.savedBytes)
        assertEquals(75.0, saved.percentageSaved, 0.001)
        assertEquals(4.0, saved.compressionRatio, 0.001)

        val larger = CompressionStatsCalculator.calculate(1_000, 1_250)
        assertEquals(-250, larger.savedBytes)
        assertEquals(-25.0, larger.percentageSaved, 0.001)
    }

    @Test
    fun targetSizeValidationRejectsInvalidCustomValues() {
        assertFalse(TargetSizeValidator.validate(TargetSize(TargetSizePreset.CUSTOM, "")).isValid)
        assertFalse(TargetSizeValidator.validate(TargetSize(TargetSizePreset.CUSTOM, "0")).isValid)
        assertFalse(TargetSizeValidator.validate(TargetSize(TargetSizePreset.CUSTOM, "5")).isValid)
        assertTrue(TargetSizeValidator.validate(TargetSize(TargetSizePreset.CUSTOM, "200")).isValid)
    }

    @Test
    fun resizeCalculatorMaintainsAspectRatioAndRejectsUpscale() {
        val half = ResizeCalculator.calculate(4000, 3000, ResizeConfig(mode = ResizeMode.PERCENT_50))
        assertEquals(2000, half.dimension?.width)
        assertEquals(1500, half.dimension?.height)

        val width = ResizeCalculator.aspectHeight(width = 1000, originalWidth = 4000, originalHeight = 3000)
        assertEquals(750, width)

        val upscale = ResizeCalculator.calculate(
            800,
            600,
            ResizeConfig(
                mode = ResizeMode.CUSTOM,
                customWidth = "1600",
                customHeight = "1200",
                allowUpscale = false,
            ),
        )
        assertFalse(upscale.validation.isValid)
        assertTrue(upscale.upscales)
    }

    @Test
    fun outputFilenameGenerationSanitizesAndMapsExtension() {
        val name = OutputFilenameGenerator.generate(
            originalName = "summer photo!!.png",
            format = ImageFormat.WEBP,
            timestampMillis = 0L,
        )
        assertTrue(name.startsWith("summer_photo_compressed_"))
        assertTrue(name.endsWith(".webp"))
        assertEquals("avatar.jpg", OutputFilenameGenerator.ensureExtension("avatar", ImageFormat.JPEG))
        assertEquals("secret_name.jpg", OutputFilenameGenerator.ensureExtension("../secret name", ImageFormat.JPEG))
        assertFalse(OutputFilenameGenerator.generate("../secret.png", ImageFormat.PNG, timestampMillis = 0L).contains(".."))
    }

    @Test
    fun privacySensitiveXmlRulesAreScoped() {
        val fileProviderPaths = readProjectFile("src/main/res/xml/file_paths.xml")
        assertTrue(fileProviderPaths.contains("path=\"processed/\""))
        assertTrue(fileProviderPaths.contains("path=\"background_removal/\""))
        assertFalse(fileProviderPaths.contains("<root-path"))
        assertFalse(fileProviderPaths.contains("<external-path"))

        val backupRules = readProjectFile("src/main/res/xml/backup_rules.xml")
        val extractionRules = readProjectFile("src/main/res/xml/data_extraction_rules.xml")
        val historyPath = "datastore/processed_image_history.preferences_pb"
        assertTrue(backupRules.contains(historyPath))
        assertTrue(extractionRules.contains(historyPath))
    }

    @Test
    fun imageFormatMapperUsesHeadersBeforeMime() {
        val jpeg = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
        assertEquals(ImageFormat.JPEG, ImageFormatMapper.fromHeader(jpeg, "image/png"))

        val webp = byteArrayOf(
            'R'.code.toByte(),
            'I'.code.toByte(),
            'F'.code.toByte(),
            'F'.code.toByte(),
            0,
            0,
            0,
            0,
            'W'.code.toByte(),
            'E'.code.toByte(),
            'B'.code.toByte(),
            'P'.code.toByte(),
        )
        assertEquals(ImageFormat.WEBP, ImageFormatMapper.fromHeader(webp, null))
    }

    @Test
    fun adPlacementPolicyAllowsConfiguredBannerAndInterstitialPlacements() {
        val policy = AdPlacementPolicy()

        BannerPlacement.entries.forEach { placement ->
            assertTrue(policy.isBannerEligible(placement))
        }
        assertTrue(policy.isInterstitialEligible(InterstitialPlacement.HISTORY_OPENED))
        assertTrue(policy.isInterstitialEligible(InterstitialPlacement.SAVE_CLICKED))
    }

    @Test
    fun historyInlineAdsAppearAfterEveryFiveContentItemsOnly() {
        val mixedItems = historyListItems((1..6).map { testProcessedImage("item-$it") })

        assertEquals(7, mixedItems.size)
        assertTrue(mixedItems[0] is HistoryListItem.Content)
        assertTrue(mixedItems[4] is HistoryListItem.Content)
        assertTrue(mixedItems[5] is HistoryListItem.Ad)
        assertTrue(mixedItems[6] is HistoryListItem.Content)
        assertEquals(0, historyListItems(emptyList()).size)
    }

    @Test
    fun qualitySearchFindsHighestQualityWithinTolerance() {
        val result = AdaptiveCompressionPlanner.searchHighestQuality(
            targetBytes = 1_000,
            minQuality = 70,
        ) { quality ->
            ByteArray(500 + quality * 6)
        }

        assertTrue(result.targetReached)
        assertEquals(91, result.quality)
        assertTrue(result.sizeBytes <= AdaptiveCompressionPlanner.toleratedTargetBytes(1_000))
    }

    @Test
    fun qualitySearchDoesNotGoBelowMinimumQualityForUnachievableTarget() {
        val result = AdaptiveCompressionPlanner.searchHighestQuality(
            targetBytes = 100,
            minQuality = 74,
        ) { quality ->
            ByteArray(1_000 + quality)
        }

        assertFalse(result.targetReached)
        assertEquals(74, result.quality)
    }

    @Test
    fun targetToleranceIsFivePercent() {
        assertTrue(AdaptiveCompressionPlanner.targetReached(105, 100))
        assertFalse(AdaptiveCompressionPlanner.targetReached(106, 100))
    }

    @Test
    fun resolutionPlanningPreservesAspectRatioAndNeverUpscales() {
        val fitted = AdaptiveCompressionPlanner.fitWithin(Dimension(8000, 4000), 4000)
        assertEquals(Dimension(4000, 2000), fitted)

        val next = AdaptiveCompressionPlanner.nextReducedDimension(
            dimension = Dimension(4000, 2000),
            resizeFactor = 0.90f,
            minShortSide = 640,
        )
        assertEquals(Dimension(3600, 1800), next)

        val tooSmall = AdaptiveCompressionPlanner.nextReducedDimension(
            dimension = Dimension(800, 640),
            resizeFactor = 0.90f,
            minShortSide = 640,
        )
        assertEquals(null, tooSmall)
    }

    @Test
    fun qualityPoliciesKeepReasonableMinimums() {
        assertTrue(AdaptiveCompressionPlanner.policyFor(CompressionMode.BEST_QUALITY).minAcceptableQuality >= 80)
        assertTrue(AdaptiveCompressionPlanner.policyFor(CompressionMode.BALANCED).minAcceptableQuality >= 70)
        assertTrue(AdaptiveCompressionPlanner.policyFor(CompressionMode.SMALLEST_SIZE).minAcceptableQuality >= 60)
        assertTrue(ImageFormat.PNG.supportsTransparency)
        assertFalse(ImageFormat.JPEG.supportsTransparency)
    }
}

private fun testProcessedImage(id: String): ProcessedImage {
    val original = ImageInfo(
        id = "original-$id",
        uriString = "content://original/$id",
        displayName = "$id.jpg",
        sizeBytes = 1000,
        width = 100,
        height = 100,
        format = ImageFormat.JPEG,
        mimeType = ImageFormat.JPEG.mimeType,
        hasAlpha = false,
    )
    return ProcessedImage(
        id = id,
        original = original,
        filePath = "/tmp/$id.jpg",
        displayName = "$id.jpg",
        sizeBytes = 500,
        width = 100,
        height = 100,
        format = ImageFormat.JPEG,
        mimeType = ImageFormat.JPEG.mimeType,
    )
}

private fun readProjectFile(relativePath: String): String {
    val moduleRelative = File(relativePath)
    if (moduleRelative.exists()) return moduleRelative.readText()
    return File("app", relativePath).readText()
}
