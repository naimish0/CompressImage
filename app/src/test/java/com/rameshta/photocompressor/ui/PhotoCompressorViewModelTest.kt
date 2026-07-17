package com.rameshta.photocompressor.ui

import com.rameshta.photocompressor.MainDispatcherRule
import com.rameshta.photocompressor.domain.model.BackgroundRemovalResult
import com.rameshta.photocompressor.domain.model.BackgroundReplacementConfig
import com.rameshta.photocompressor.domain.model.CompressionConfig
import com.rameshta.photocompressor.domain.model.HistoryOperationType
import com.rameshta.photocompressor.domain.model.ImageFormat
import com.rameshta.photocompressor.domain.model.ImageInfo
import com.rameshta.photocompressor.domain.model.ImageSource
import com.rameshta.photocompressor.domain.model.ProcessedImage
import com.rameshta.photocompressor.domain.model.SavedImage
import com.rameshta.photocompressor.domain.repository.BackgroundRemovalRepository
import com.rameshta.photocompressor.domain.repository.HistoryRepository
import com.rameshta.photocompressor.domain.repository.ImageRepository
import com.rameshta.photocompressor.domain.usecase.CompressImageUseCase
import com.rameshta.photocompressor.domain.usecase.LoadImageInfoUseCase
import com.rameshta.photocompressor.domain.usecase.RemoveBackgroundUseCase
import com.rameshta.photocompressor.domain.usecase.ReplaceBackgroundUseCase
import com.rameshta.photocompressor.domain.usecase.SaveImagesUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
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
        val historyRepository = FakeHistoryRepository()
        val viewModel = viewModel(repository, historyRepository = historyRepository)

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
        assertEquals(2, historyRepository.historyFlow.value.size)
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
        val historyRepository = FakeHistoryRepository()
        val viewModel = viewModel(
            repository,
            backgroundRepository = FakeBackgroundRemovalRepository(result = BackgroundRemovalResult.Success(processedImage("background"))),
            historyRepository = historyRepository,
        )

        viewModel.addImageUris(listOf("uri://one"))
        advanceUntilIdle()
        viewModel.removeBackground()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.backgroundState is BackgroundUiState.Success)
        assertEquals(HistoryOperationType.BACKGROUND_REMOVED, historyRepository.historyFlow.value.first().operationType)
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

    @Test
    fun successfulSaveQueuesPendingAdAndRecordsSavedHistoryAfterContinuation() = runTest {
        val repository = FakeImageRepository()
        val historyRepository = FakeHistoryRepository()
        val viewModel = viewModel(repository, historyRepository = historyRepository)

        viewModel.addImageUris(listOf("uri://one"))
        advanceUntilIdle()
        viewModel.startCompression()
        advanceUntilIdle()
        viewModel.saveSelected()
        advanceUntilIdle()

        val pending = viewModel.uiState.value.pendingAdAction as PendingAdAction.SaveResult
        assertEquals(0, repository.saveCalls)

        viewModel.performPendingSave(pending.requestId)
        advanceUntilIdle()

        assertEquals(1, repository.saveCalls)
        assertEquals("Image saved successfully", viewModel.uiState.value.message)
        assertTrue(historyRepository.historyFlow.value.first().filePath.startsWith("content://saved/"))
    }

    @Test
    fun duplicateSaveTapIsIgnoredWhileSaveIsActive() = runTest {
        val repository = FakeImageRepository(saveHang = true)
        val viewModel = viewModel(repository)

        viewModel.addImageUris(listOf("uri://one"))
        advanceUntilIdle()
        viewModel.startCompression()
        advanceUntilIdle()
        viewModel.saveSelected()
        val pending = viewModel.uiState.value.pendingAdAction as PendingAdAction.SaveResult
        viewModel.performPendingSave(pending.requestId)
        runCurrent()
        viewModel.saveSelected()
        runCurrent()

        assertEquals(1, repository.saveCalls)
        assertTrue(viewModel.uiState.value.isSaving)
        repository.releaseSave()
        advanceUntilIdle()
    }

    private fun viewModel(
        repository: FakeImageRepository,
        backgroundRepository: BackgroundRemovalRepository = FakeBackgroundRemovalRepository(),
        historyRepository: FakeHistoryRepository = FakeHistoryRepository(),
    ): PhotoCompressorViewModel {
        return PhotoCompressorViewModel(
            loadImageInfo = LoadImageInfoUseCase(repository),
            compressImage = CompressImageUseCase(repository),
            saveImages = SaveImagesUseCase(repository),
            removeBackgroundUseCase = RemoveBackgroundUseCase(backgroundRepository),
            replaceBackgroundUseCase = ReplaceBackgroundUseCase(repository),
            historyRepository = historyRepository,
        )
    }
}

private class FakeImageRepository(
    private val failIds: Set<String> = emptySet(),
    private val hangIds: Set<String> = emptySet(),
    private val progressValues: List<Float> = listOf(1f),
    private val saveHang: Boolean = false,
) : ImageRepository {
    private val saveGate = if (saveHang) CompletableDeferred<Unit>() else null
    var compressCalls: Int = 0
        private set
    var saveCalls: Int = 0
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
        saveCalls += 1
        saveGate?.await()
        return Result.success(SavedImage(uriString = "content://saved/${image.id}", displayName = requestedName ?: image.displayName))
    }

    fun releaseSave() {
        saveGate?.complete(Unit)
    }

    override suspend fun saveAll(images: List<ProcessedImage>): List<Result<SavedImage>> {
        return images.map { saveImage(it) }
    }

    override suspend fun cleanupObsoleteTempFiles() = Unit
}

private class FakeHistoryRepository : HistoryRepository {
    val historyFlow = MutableStateFlow<List<ProcessedImage>>(emptyList())
    override val history: Flow<List<ProcessedImage>> = historyFlow

    override suspend fun recordSuccessfulOutput(
        output: ProcessedImage,
        operationType: HistoryOperationType,
    ): Result<Unit> {
        historyFlow.value = (listOf(output.copy(operationType = operationType)) + historyFlow.value)
            .distinctBy { it.id }
        return Result.success(Unit)
    }

    override suspend fun remove(id: String) {
        historyFlow.value = historyFlow.value.filterNot { it.id == id }
    }

    override suspend fun clear() {
        historyFlow.value = emptyList()
    }
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
