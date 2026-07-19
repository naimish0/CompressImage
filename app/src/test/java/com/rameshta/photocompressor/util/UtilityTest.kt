package com.rameshta.photocompressor.util

import com.rameshta.photocompressor.ads.AdPlacementPolicy
import com.rameshta.photocompressor.ads.BannerPlacement
import com.rameshta.photocompressor.ads.FullscreenAdCoordinator
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
import com.rameshta.photocompressor.ui.history.historyListItems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class UtilityTest {
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
        assertTrue(TargetSizeValidator.validate(TargetSize(TargetSizePreset.CUSTOM, "10,5")).isValid)
        assertTrue(TargetSizeValidator.validate(TargetSize(TargetSizePreset.CUSTOM, "١٠٫٥")).isValid)
        assertTrue(TargetSizeValidator.validate(TargetSize(TargetSizePreset.CUSTOM, "१०.५")).isValid)
        assertFalse(TargetSizeValidator.validate(TargetSize(TargetSizePreset.CUSTOM, "10,5.2")).isValid)
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

        val allowedUpscale = ResizeCalculator.calculate(
            800,
            600,
            ResizeConfig(
                mode = ResizeMode.CUSTOM,
                customWidth = "1600",
                customHeight = "1200",
                allowUpscale = true,
            ),
        )
        assertTrue(allowedUpscale.validation.isValid)
        assertEquals(Dimension(1600, 1200), allowedUpscale.dimension)

        val localizedDigits = ResizeCalculator.calculate(
            4000,
            3000,
            ResizeConfig(
                mode = ResizeMode.CUSTOM,
                customWidth = "١٢٠٠",
                customHeight = "٩٠٠",
                maintainAspectRatio = false,
            ),
        )
        assertTrue(localizedDigits.validation.isValid)
        assertEquals(Dimension(1200, 900), localizedDigits.dimension)
    }

    @Test
    fun customAspectLockedDimensionsAreABoundingBoxForMixedAspectBatches() {
        val resize = ResizeConfig(
            mode = ResizeMode.CUSTOM,
            customWidth = "1200",
            customHeight = "900",
            maintainAspectRatio = true,
        )

        val landscape = ResizeCalculator.calculate(4000, 3000, resize)
        val portrait = ResizeCalculator.calculate(2000, 4000, resize)

        assertEquals(Dimension(1200, 900), landscape.dimension)
        assertEquals(Dimension(450, 900), portrait.dimension)
        assertTrue(landscape.validation.isValid)
        assertTrue(portrait.validation.isValid)
    }

    @Test
    fun customDimensionsCanStillStretchWhenAspectLockIsOff() {
        val result = ResizeCalculator.calculate(
            2000,
            4000,
            ResizeConfig(
                mode = ResizeMode.CUSTOM,
                customWidth = "1200",
                customHeight = "900",
                maintainAspectRatio = false,
            ),
        )

        assertEquals(Dimension(1200, 900), result.dimension)
        assertTrue(result.validation.isValid)
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
    fun adPlacementPolicyAllowsOneAnchoredBannerAndNaturalInterstitialPlacement() {
        val policy = AdPlacementPolicy()

        BannerPlacement.entries.forEach { placement ->
            assertEquals(
                placement in setOf(
                    BannerPlacement.TOP,
                    BannerPlacement.BOTTOM,
                    BannerPlacement.HOME_EMPTY_SPACE,
                    BannerPlacement.RESULT_EMPTY_SPACE,
                ),
                policy.isBannerEligible(placement),
            )
        }
        assertTrue(policy.isInterstitialEligible(InterstitialPlacement.WORKFLOW_COMPLETED))
    }

    @Test
    fun historyListContainsContentOnly() {
        val mixedItems = historyListItems((1..6).map { testProcessedImage("item-$it") })

        assertEquals(6, mixedItems.size)
        assertEquals("item-1", mixedItems[0].id)
        assertEquals("item-5", mixedItems[4].id)
        assertEquals("item-6", mixedItems[5].id)
        assertEquals(0, historyListItems(emptyList()).size)
    }

    @Test
    fun fullscreenAdCoordinatorConsumesAppOpenSuppressionOnce() {
        val coordinator = FullscreenAdCoordinator()

        coordinator.suppressNextAppOpen()

        assertTrue(coordinator.consumeAppOpenSuppression())
        assertFalse(coordinator.consumeAppOpenSuppression())
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
