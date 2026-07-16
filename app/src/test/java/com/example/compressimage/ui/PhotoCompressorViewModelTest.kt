package com.example.compressimage.ui

import com.example.compressimage.MainDispatcherRule
import com.example.compressimage.ads.AdFrequencyCapper
import com.example.compressimage.domain.model.BackgroundRemovalResult
import com.example.compressimage.domain.model.BackgroundReplacementConfig
import com.example.compressimage.domain.model.CompressionConfig
import com.example.compressimage.domain.model.ImageFormat
import com.example.compressimage.domain.model.ImageInfo
import com.example.compressimage.domain.model.ImageSource
import com.example.compressimage.domain.model.ProcessedImage
import com.example.compressimage.domain.model.SavedImage
import com.example.compressimage.domain.repository.BackgroundRemovalRepository
import com.example.compressimage.domain.repository.ImageRepository
import com.example.compressimage.domain.usecase.CompressImageUseCase
import com.example.compressimage.domain.usecase.LoadImageInfoUseCase
import com.example.compressimage.domain.usecase.RemoveBackgroundUseCase
import com.example.compressimage.domain.usecase.ReplaceBackgroundUseCase
import com.example.compressimage.domain.usecase.SaveImagesUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class PhotoCompressorViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun addImagesAndCompressSuccessUpdatesResultsAndSummary() = runTest {
        val repository = FakeImageRepository()
        val viewModel = viewModel(repository)

        viewModel.addImageUris(listOf("uri://one", "uri://two"))
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.selectedImages.size)

        viewModel.startCompression()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.batch.isRunning)
        assertEquals(2, state.results.size)
        assertEquals(2, state.batch.summary?.successful)
        assertEquals(0, state.batch.summary?.failed)
    }

    @Test
    fun batchContinuesWhenOneImageFails() = runTest {
        val repository = FakeImageRepository(failIds = setOf("two"))
        val viewModel = viewModel(repository)

        viewModel.addImageUris(listOf("uri://one", "uri://two", "uri://three"))
        advanceUntilIdle()
        viewModel.startCompression()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.results.size)
        assertEquals(2, state.batch.summary?.successful)
        assertEquals(1, state.batch.summary?.failed)
        assertTrue(state.batch.items.any { it.status == BatchItemStatus.FAILED })
    }

    @Test
    fun cancellationMarksRemainingItemsCancelled() = runTest {
        val repository = FakeImageRepository(hangIds = setOf("one"))
        val viewModel = viewModel(repository)

        viewModel.addImageUris(listOf("uri://one", "uri://two"))
        advanceUntilIdle()
        viewModel.startCompression()
        viewModel.cancelCompression()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.batch.isRunning)
        assertTrue(state.batch.summary?.cancelled == true)
        assertTrue(state.batch.items.any { it.status == BatchItemStatus.CANCELLED })
    }

    @Test
    fun duplicateCompressTapDoesNotStartASecondJob() = runTest {
        val repository = FakeImageRepository(hangIds = setOf("one"))
        val viewModel = viewModel(repository)

        viewModel.addImageUris(listOf("uri://one"))
        advanceUntilIdle()
        viewModel.startCompression()
        viewModel.startCompression()
        advanceUntilIdle()

        assertEquals(1, repository.compressCalls)
        assertEquals(ProcessingStage.OPTIMIZING_RESOLUTION, viewModel.uiState.value.batch.items.first().stage)
        viewModel.cancelCompression()
    }

    @Test
    fun runningProgressUpdatesProcessingStage() = runTest {
        val repository = FakeImageRepository(progressValues = listOf(0.2f, 0.5f, 0.95f, 1f))
        val viewModel = viewModel(repository)

        viewModel.addImageUris(listOf("uri://one"))
        advanceUntilIdle()
        viewModel.startCompression()
        advanceUntilIdle()

        assertEquals(ProcessingStage.COMPLETE, viewModel.uiState.value.batch.items.first().stage)
    }


    @Test
    fun backgroundRemovalSuccessStateIsExposed() = runTest {
        val repository = FakeImageRepository()
        val viewModel = viewModel(
            repository,
            backgroundRepository = FakeBackgroundRemovalRepository(result = BackgroundRemovalResult.Success(processedImage("background"))),
        )

        viewModel.addImageUris(listOf("uri://one"))
        advanceUntilIdle()
        viewModel.removeBackground()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.backgroundState is BackgroundUiState.Success)
    }

    @Test
    fun duplicateBackgroundRemovalTapDoesNotStartSecondJob() = runTest {
        val repository = FakeImageRepository()
        val backgroundRepository = FakeBackgroundRemovalRepository(hang = true)
        val viewModel = viewModel(repository, backgroundRepository)

        viewModel.addImageUris(listOf("uri://one"))
        advanceUntilIdle()
        viewModel.removeBackground()
        viewModel.removeBackground()
        advanceUntilIdle()

        assertEquals(1, backgroundRepository.calls)
        assertTrue(viewModel.uiState.value.backgroundState is BackgroundUiState.Running)
        viewModel.cancelBackgroundRemoval()
    }

    @Test
    fun cancelBackgroundRemovalShowsCancelledState() = runTest {
        val repository = FakeImageRepository()
        val backgroundRepository = FakeBackgroundRemovalRepository(hang = true)
        val viewModel = viewModel(repository, backgroundRepository)

        viewModel.addImageUris(listOf("uri://one"))
        advanceUntilIdle()
        viewModel.removeBackground()
        viewModel.cancelBackgroundRemoval()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.backgroundState is BackgroundUiState.Cancelled)
    }

    private fun viewModel(
        repository: FakeImageRepository,
        backgroundRepository: BackgroundRemovalRepository = FakeBackgroundRemovalRepository(),
    ): PhotoCompressorViewModel {
        return PhotoCompressorViewModel(
            loadImageInfo = LoadImageInfoUseCase(repository),
            compressImage = CompressImageUseCase(repository),
            saveImages = SaveImagesUseCase(repository),
            removeBackgroundUseCase = RemoveBackgroundUseCase(backgroundRepository),
            replaceBackgroundUseCase = ReplaceBackgroundUseCase(repository),
            adFrequencyCapper = AdFrequencyCapper(),
        )
    }
}

private class FakeImageRepository(
    private val failIds: Set<String> = emptySet(),
    private val hangIds: Set<String> = emptySet(),
    private val progressValues: List<Float> = listOf(1f),
) : ImageRepository {
    var compressCalls: Int = 0
        private set

    override suspend fun loadImageInfo(uriString: String): Result<ImageInfo> {
        val id = uriString.substringAfterLast("/")
        return Result.success(
            ImageInfo(
                id = id,
                uriString = uriString,
                displayName = "$id.jpg",
                sizeBytes = 1_000_000,
                width = 4000,
                height = 3000,
                format = ImageFormat.JPEG,
                mimeType = ImageFormat.JPEG.mimeType,
                hasAlpha = false,
            ),
        )
    }

    override suspend fun compressImage(
        image: ImageInfo,
        config: CompressionConfig,
        progress: (Float) -> Unit,
    ): Result<ProcessedImage> {
        compressCalls += 1
        if (image.id in hangIds) {
            progress(0.25f)
            suspendCancellableCoroutine<Nothing> { }
        }
        if (image.id in failIds) {
            return Result.failure(IOException("Could not compress ${image.displayName}"))
        }
        progressValues.forEach(progress)
        return Result.success(
            ProcessedImage(
                id = "processed-${image.id}",
                original = image,
                filePath = "/tmp/${image.id}.jpg",
                displayName = "${image.id}-compressed.jpg",
                sizeBytes = 200_000,
                width = 1600,
                height = 1200,
                format = config.outputFormat,
                mimeType = config.outputFormat.mimeType,
                requestedTargetBytes = config.targetSize.bytesOrNull(),
                targetReached = true,
                outputQuality = 90,
                compressionMode = config.compressionMode,
            ),
        )
    }

    override suspend fun replaceBackground(
        image: ProcessedImage,
        config: BackgroundReplacementConfig,
        progress: (Float) -> Unit,
    ): Result<ProcessedImage> {
        progress(1f)
        return Result.success(image)
    }

    override suspend fun saveImage(image: ProcessedImage, requestedName: String?): Result<SavedImage> {
        return Result.success(SavedImage(uriString = "content://saved/${image.id}", displayName = requestedName ?: image.displayName))
    }

    override suspend fun saveAll(images: List<ProcessedImage>): List<Result<SavedImage>> {
        return images.map { saveImage(it) }
    }

    override suspend fun cleanupObsoleteTempFiles() = Unit
}

private fun processedImage(id: String): ProcessedImage {
    val original = ImageInfo(
        id = id,
        uriString = "uri://$id",
        displayName = "$id.png",
        sizeBytes = 1_000,
        width = 100,
        height = 100,
        format = ImageFormat.PNG,
        mimeType = ImageFormat.PNG.mimeType,
        hasAlpha = true,
    )
    return ProcessedImage(
        id = id,
        original = original,
        filePath = "/tmp/$id.png",
        displayName = "$id.png",
        sizeBytes = 500,
        width = 100,
        height = 100,
        format = ImageFormat.PNG,
        mimeType = ImageFormat.PNG.mimeType,
    )
}

private class FakeBackgroundRemovalRepository(
    private val result: BackgroundRemovalResult = BackgroundRemovalResult.Unavailable("No provider configured."),
    private val hang: Boolean = false,
) : BackgroundRemovalRepository {
    var calls: Int = 0
        private set

    override suspend fun removeBackground(
        input: ImageSource,
        progress: (Float) -> Unit,
    ): BackgroundRemovalResult {
        calls += 1
        progress(0.2f)
        if (hang) {
            suspendCancellableCoroutine<Nothing> { }
        }
        progress(1f)
        return result
    }
}
