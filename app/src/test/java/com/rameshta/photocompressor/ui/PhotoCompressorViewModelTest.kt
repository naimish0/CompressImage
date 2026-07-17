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
import com.rameshta.photocompressor.ui.history.HistoryUiState
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.advanceTimeBy
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
        assertEquals(2, state.selectedImages.size)
        assertTrue(state.visibleSelectedImages.isEmpty())
        assertEquals(2, state.results.size)
        assertEquals(2, state.batch.summary?.successful)
        assertEquals(0, state.batch.summary?.failed)
        assertEquals(2, historyRepository.historyFlow.value.size)
        assertTrue(viewModel.uiState.value.history.isNotEmpty())
        assertTrue(viewModel.historyUiState.value is HistoryUiState.Content)
    }

    @Test
    fun historyStateTransitionsFromLoadingToEmpty() = runTest {
        val historyRepository = ControlledHistoryRepository()
        val viewModel = viewModel(FakeImageRepository(), historyRepository = historyRepository)

        assertEquals(HistoryUiState.Loading, viewModel.historyUiState.value)
        runCurrent()
        assertEquals(HistoryUiState.Loading, viewModel.historyUiState.value)
        historyRepository.emit(emptyList())
        runCurrent()

        assertEquals(HistoryUiState.Empty, viewModel.historyUiState.value)
    }

    @Test
    fun historyStateTransitionsFromLoadingToContent() = runTest {
        val history = listOf(processedImage("history"))
        val historyRepository = ControlledHistoryRepository()
        val viewModel = viewModel(
            FakeImageRepository(),
            historyRepository = historyRepository,
        )

        assertEquals(HistoryUiState.Loading, viewModel.historyUiState.value)
        runCurrent()
        assertEquals(HistoryUiState.Loading, viewModel.historyUiState.value)
        historyRepository.emit(history)
        runCurrent()

        assertEquals(HistoryUiState.Content(history), viewModel.historyUiState.value)
        assertEquals(history, viewModel.uiState.value.history)
    }

    @Test
    fun historyRepositoryErrorShowsRecoverableErrorState() = runTest {
        val viewModel = viewModel(
            FakeImageRepository(),
            historyRepository = RecoveringHistoryRepository(initiallyFailing = true),
        )

        advanceUntilIdle()

        val state = viewModel.historyUiState.value
        assertTrue(state is HistoryUiState.Error)
        assertTrue((state as HistoryUiState.Error).message.contains("History unavailable"))
    }

    @Test
    fun refreshHistoryRestartsCollectionAfterRepositoryError() = runTest {
        val historyRepository = RecoveringHistoryRepository(initiallyFailing = true)
        val viewModel = viewModel(FakeImageRepository(), historyRepository = historyRepository)
        advanceUntilIdle()
        assertTrue(viewModel.historyUiState.value is HistoryUiState.Error)

        val restored = listOf(processedImage("restored"))
        historyRepository.fail = false
        historyRepository.historyFlow.value = restored
        viewModel.refreshHistory()
        advanceUntilIdle()

        assertEquals(HistoryUiState.Content(restored), viewModel.historyUiState.value)
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
        assertEquals(listOf("one", "two", "three"), state.selectedImages.map { it.id })
        assertEquals(listOf("two"), state.visibleSelectedImages.map { it.id })
        assertEquals(2, state.batch.summary?.successful)
        assertEquals(1, state.batch.summary?.failed)
        assertTrue(state.batch.items.any { it.status == BatchItemStatus.FAILED })
    }

    @Test
    fun cancellationMarksRemainingItemsCancelled() = runTest {
        val repository = FakeImageRepository(hangIds = setOf("one"))
        val historyRepository = FakeHistoryRepository()
        val viewModel = viewModel(repository, historyRepository = historyRepository)

        viewModel.addImageUris(listOf("uri://one", "uri://two"))
        advanceUntilIdle()
        viewModel.startCompression()
        viewModel.cancelCompression()
        advanceUntilIdle()
        val restored = listOf(processedImage("after-cancel"))
        historyRepository.historyFlow.value = restored
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.batch.isRunning)
        assertTrue(state.batch.summary?.cancelled == true)
        assertTrue(state.batch.items.any { it.status == BatchItemStatus.CANCELLED })
        assertEquals(HistoryUiState.Content(restored), viewModel.historyUiState.value)
    }

    @Test
    fun recreatedViewModelRestoresHistoryFromRepository() = runTest {
        val historyRepository = FakeHistoryRepository(initialHistory = listOf(processedImage("saved")))
        val first = viewModel(FakeImageRepository(), historyRepository = historyRepository)
        advanceUntilIdle()
        assertTrue(first.historyUiState.value is HistoryUiState.Content)

        val recreated = viewModel(FakeImageRepository(), historyRepository = historyRepository)
        advanceUntilIdle()

        assertEquals(first.uiState.value.history, recreated.uiState.value.history)
        assertEquals(first.historyUiState.value, recreated.historyUiState.value)
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
    fun compressionUsesShortProcessingTransitionDelay() = runTest {
        val repository = FakeImageRepository()
        val viewModel = viewModel(repository)

        viewModel.addImageUris(listOf("uri://one"))
        advanceUntilIdle()

        viewModel.startCompression()
        runCurrent()

        assertTrue(viewModel.uiState.value.batch.isRunning)
        assertEquals(0, repository.compressCalls)

        advanceTimeBy(701)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.batch.isRunning)
        assertEquals(1, repository.compressCalls)
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
    fun backgroundRemovalUsesShortProcessingTransitionDelay() = runTest {
        val backgroundRepository = FakeBackgroundRemovalRepository(
            result = BackgroundRemovalResult.Success(processedImage("background")),
        )
        val viewModel = viewModel(FakeImageRepository(), backgroundRepository)

        viewModel.addImageUris(listOf("uri://one"))
        advanceUntilIdle()

        viewModel.removeBackground()
        runCurrent()

        assertTrue(viewModel.uiState.value.backgroundState is BackgroundUiState.Running)
        assertEquals(0, backgroundRepository.calls)

        advanceTimeBy(701)
        advanceUntilIdle()

        assertEquals(1, backgroundRepository.calls)
        assertTrue(viewModel.uiState.value.backgroundState is BackgroundUiState.Success)
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
        assertEquals(1, viewModel.uiState.value.selectedImages.size)
        assertTrue(viewModel.uiState.value.visibleSelectedImages.isEmpty())
        assertTrue(historyRepository.historyFlow.value.isEmpty())
        assertEquals(HistoryUiState.Empty, viewModel.historyUiState.value)
    }

    @Test
    fun backgroundExportSelectsResultAndRequestsResultNavigation() = runTest {
        val repository = FakeImageRepository()
        val historyRepository = FakeHistoryRepository()
        val viewModel = viewModel(
            repository,
            backgroundRepository = FakeBackgroundRemovalRepository(
                result = BackgroundRemovalResult.Success(processedImage("background")),
            ),
            historyRepository = historyRepository,
        )

        viewModel.addImageUris(listOf("uri://one"))
        advanceUntilIdle()
        viewModel.removeBackground()
        advanceUntilIdle()
        viewModel.replaceBackground(BackgroundReplacementConfig.Transparent)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("background", state.selectedResultId)
        assertEquals("background", state.pendingResultNavigationId)
        assertEquals(1, state.results.size)
        assertEquals("background", state.results.first().id)
        assertEquals(0, repository.replaceCalls)
        assertEquals(1, historyRepository.historyFlow.value.size)
        assertEquals("background", historyRepository.historyFlow.value.first().id)

        viewModel.consumePendingResultNavigation()

        assertEquals(null, viewModel.uiState.value.pendingResultNavigationId)
    }

    @Test
    fun openHistoryItemReturnsFalseForMissingItem() = runTest {
        val viewModel = viewModel(FakeImageRepository())
        advanceUntilIdle()

        assertFalse(viewModel.openHistoryItem("missing"))
        assertTrue(viewModel.uiState.value.results.isEmpty())
    }

    @Test
    fun openHistoryItemSelectsExistingItem() = runTest {
        val item = processedImage("existing")
        val viewModel = viewModel(
            FakeImageRepository(),
            historyRepository = FakeHistoryRepository(initialHistory = listOf(item)),
        )
        advanceUntilIdle()

        assertTrue(viewModel.openHistoryItem(item.id))

        assertEquals(item.id, viewModel.uiState.value.selectedResultId)
        assertEquals(1, viewModel.uiState.value.results.size)
        assertEquals(item.id, viewModel.uiState.value.results.first().id)
    }

    @Test
    fun duplicateHistoryRequestsAreIgnoredWhilePending() = runTest {
        val viewModel = viewModel(FakeImageRepository())

        viewModel.requestOpenHistory()
        viewModel.requestOpenHistory()

        assertEquals(PendingAdAction.OpenHistory, viewModel.uiState.value.pendingAdAction)
    }

    @Test
    fun previewBackThenHistoryRequestKeepsExistingHistoryVisible() = runTest {
        val history = listOf(processedImage("existing"))
        val viewModel = viewModel(
            FakeImageRepository(),
            historyRepository = FakeHistoryRepository(initialHistory = history),
        )

        viewModel.addImageUris(listOf("uri://selected"))
        advanceUntilIdle()
        viewModel.requestOpenHistory()
        viewModel.requestOpenHistory()

        assertEquals(1, viewModel.uiState.value.selectedImages.size)
        assertEquals(PendingAdAction.OpenHistory, viewModel.uiState.value.pendingAdAction)
        assertEquals(HistoryUiState.Content(history), viewModel.historyUiState.value)

        viewModel.consumePendingAdAction()

        assertEquals(PendingAdAction.None, viewModel.uiState.value.pendingAdAction)
        assertEquals(HistoryUiState.Content(history), viewModel.historyUiState.value)
    }

    @Test
    fun previewBackThenHistoryRequestKeepsEmptyHistoryVisible() = runTest {
        val viewModel = viewModel(FakeImageRepository())

        viewModel.addImageUris(listOf("uri://selected"))
        advanceUntilIdle()
        viewModel.requestOpenHistory()

        assertEquals(1, viewModel.uiState.value.selectedImages.size)
        assertEquals(PendingAdAction.OpenHistory, viewModel.uiState.value.pendingAdAction)
        assertEquals(HistoryUiState.Empty, viewModel.historyUiState.value)
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
    fun saveSelectedAfterInterstitialPerformsSaveWithoutQueuingAnotherAd() = runTest {
        val repository = FakeImageRepository()
        val historyRepository = FakeHistoryRepository()
        val viewModel = viewModel(repository, historyRepository = historyRepository)

        viewModel.addImageUris(listOf("uri://one"))
        advanceUntilIdle()
        viewModel.startCompression()
        advanceUntilIdle()

        viewModel.saveSelectedAfterInterstitial("custom-name.jpg")
        advanceUntilIdle()

        assertEquals(PendingAdAction.None, viewModel.uiState.value.pendingAdAction)
        assertEquals(1, repository.saveCalls)
        assertEquals("Image saved successfully", viewModel.uiState.value.message)
        assertEquals("custom-name.jpg", historyRepository.historyFlow.value.first().displayName)
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
        historyRepository: HistoryRepository = FakeHistoryRepository(),
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
    var replaceCalls: Int = 0
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
        replaceCalls += 1
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

private class FakeHistoryRepository(
    initialHistory: List<ProcessedImage> = emptyList(),
) : HistoryRepository {
    val historyFlow = MutableStateFlow(initialHistory)
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

private class ControlledHistoryRepository : HistoryRepository {
    private val emissions = MutableSharedFlow<List<ProcessedImage>>()
    override val history: Flow<List<ProcessedImage>> = emissions

    suspend fun emit(history: List<ProcessedImage>) {
        emissions.emit(history)
    }

    override suspend fun recordSuccessfulOutput(
        output: ProcessedImage,
        operationType: HistoryOperationType,
    ): Result<Unit> {
        emit(listOf(output.copy(operationType = operationType)))
        return Result.success(Unit)
    }

    override suspend fun remove(id: String) = Unit

    override suspend fun clear() = Unit
}

private class RecoveringHistoryRepository(
    initiallyFailing: Boolean,
) : HistoryRepository {
    val historyFlow = MutableStateFlow<List<ProcessedImage>>(emptyList())
    var fail: Boolean = initiallyFailing

    override val history: Flow<List<ProcessedImage>>
        get() = flow {
            if (fail) {
                throw IOException("History unavailable")
            }
            emit(historyFlow.value)
        }

    override suspend fun recordSuccessfulOutput(
        output: ProcessedImage,
        operationType: HistoryOperationType,
    ): Result<Unit> {
        historyFlow.value = listOf(output.copy(operationType = operationType)) + historyFlow.value
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
