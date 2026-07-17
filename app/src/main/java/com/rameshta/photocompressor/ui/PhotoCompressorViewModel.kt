package com.rameshta.photocompressor.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rameshta.photocompressor.domain.model.BackgroundRemovalResult
import com.rameshta.photocompressor.domain.model.BackgroundReplacementConfig
import com.rameshta.photocompressor.domain.model.CompressionConfig
import com.rameshta.photocompressor.domain.model.CompressionMode
import com.rameshta.photocompressor.domain.model.Dimension
import com.rameshta.photocompressor.domain.model.HistoryOperationType
import com.rameshta.photocompressor.domain.model.ImageFormat
import com.rameshta.photocompressor.domain.model.ImageInfo
import com.rameshta.photocompressor.domain.model.ImageSource
import com.rameshta.photocompressor.domain.model.ProcessedImage
import com.rameshta.photocompressor.domain.model.ProcessingSummary
import com.rameshta.photocompressor.domain.model.ResizeConfig
import com.rameshta.photocompressor.domain.model.ResizeMode
import com.rameshta.photocompressor.domain.model.SavedImage
import com.rameshta.photocompressor.domain.model.TargetSizePreset
import com.rameshta.photocompressor.domain.model.TargetSizeUnit
import com.rameshta.photocompressor.domain.repository.HistoryRepository
import com.rameshta.photocompressor.domain.usecase.CompressImageUseCase
import com.rameshta.photocompressor.domain.usecase.LoadImageInfoUseCase
import com.rameshta.photocompressor.domain.usecase.RemoveBackgroundUseCase
import com.rameshta.photocompressor.domain.usecase.ReplaceBackgroundUseCase
import com.rameshta.photocompressor.domain.usecase.SaveImagesUseCase
import com.rameshta.photocompressor.util.ResizeCalculator
import com.rameshta.photocompressor.util.TargetSizeValidator
import com.rameshta.photocompressor.util.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class PhotoCompressorViewModel @Inject constructor(
    private val loadImageInfo: LoadImageInfoUseCase,
    private val compressImage: CompressImageUseCase,
    private val saveImages: SaveImagesUseCase,
    private val removeBackgroundUseCase: RemoveBackgroundUseCase,
    private val replaceBackgroundUseCase: ReplaceBackgroundUseCase,
    private val historyRepository: HistoryRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PhotoCompressorUiState())
    val uiState: StateFlow<PhotoCompressorUiState> = _uiState.asStateFlow()

    private var compressionJob: Job? = null
    private var backgroundJob: Job? = null
    private val pendingSaveRequests = mutableMapOf<String, PendingSaveRequest>()

    init {
        viewModelScope.launch {
            historyRepository.history.collect { history ->
                _uiState.update { it.copy(history = history) }
            }
        }
    }

    fun addImageUris(uriStrings: List<String>) {
        val unique = uriStrings.distinct().filterNot { incoming ->
            _uiState.value.selectedImages.any { it.uriString == incoming }
        }
        if (unique.isEmpty()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingSelection = true,
                    selectionError = null,
                    message = null,
                )
            }
            val loaded = mutableListOf<ImageInfo>()
            val failures = mutableListOf<String>()
            unique.forEach { uri ->
                loadImageInfo(uri)
                    .onSuccess { loaded += it }
                    .onFailure { failures += (it.message ?: "Could not read one image.") }
            }
            _uiState.update { state ->
                state.copy(
                    selectedImages = state.selectedImages + loaded,
                    isLoadingSelection = false,
                    selectionError = failures.distinct().joinToString("\n").ifBlank { null },
                )
            }
            updateResizeFromCurrentSelectionIfNeeded()
        }
    }

    fun removeImage(id: String) {
        _uiState.update { state ->
            state.copy(selectedImages = state.selectedImages.filterNot { it.id == id })
        }
    }

    fun clearSelection() {
        compressionJob?.cancel()
        _uiState.update {
            PhotoCompressorUiState(
                keepOriginal = it.keepOriginal,
                history = it.history,
            )
        }
    }

    fun updateTargetPreset(preset: TargetSizePreset) {
        _uiState.update { state ->
            state.copy(config = state.config.copy(targetSize = state.config.targetSize.copy(preset = preset)))
        }
    }

    fun updateCustomTarget(value: String) {
        _uiState.update { state ->
            state.copy(config = state.config.copy(targetSize = state.config.targetSize.copy(customValue = value)))
        }
    }

    fun updateCustomTargetUnit(unit: TargetSizeUnit) {
        _uiState.update { state ->
            state.copy(config = state.config.copy(targetSize = state.config.targetSize.copy(customUnit = unit)))
        }
    }

    fun updateOutputFormat(format: ImageFormat) {
        if (format == ImageFormat.UNKNOWN) return
        _uiState.update { state -> state.copy(config = state.config.copy(outputFormat = format)) }
    }

    fun updateCompressionMode(mode: CompressionMode) {
        _uiState.update { state -> state.copy(config = state.config.copy(compressionMode = mode)) }
    }

    fun updateResizeMode(mode: ResizeMode) {
        _uiState.update { state ->
            val first = state.selectedImages.firstOrNull()
            val resize = when (mode) {
                ResizeMode.CUSTOM -> {
                    val width = state.config.resize.customWidth.ifBlank { first?.width?.toString().orEmpty() }
                    val height = state.config.resize.customHeight.ifBlank { first?.height?.toString().orEmpty() }
                    state.config.resize.copy(mode = mode, customWidth = width, customHeight = height)
                }
                else -> state.config.resize.copy(mode = mode)
            }
            state.copy(config = state.config.copy(resize = resize))
        }
    }

    fun updateCustomWidth(value: String) {
        _uiState.update { state ->
            val first = state.selectedImages.firstOrNull()
            val width = value.filter { it.isDigit() }.take(5)
            val height = if (state.config.resize.maintainAspectRatio && first != null) {
                width.toIntOrNull()
                    ?.let { ResizeCalculator.aspectHeight(it, first.width, first.height).toString() }
                    .orEmpty()
            } else {
                state.config.resize.customHeight
            }
            state.copy(
                config = state.config.copy(
                    resize = state.config.resize.copy(
                        mode = ResizeMode.CUSTOM,
                        customWidth = width,
                        customHeight = height,
                    ),
                ),
            )
        }
    }

    fun updateCustomHeight(value: String) {
        _uiState.update { state ->
            val first = state.selectedImages.firstOrNull()
            val height = value.filter { it.isDigit() }.take(5)
            val width = if (state.config.resize.maintainAspectRatio && first != null) {
                height.toIntOrNull()
                    ?.let { ResizeCalculator.aspectWidth(it, first.width, first.height).toString() }
                    .orEmpty()
            } else {
                state.config.resize.customWidth
            }
            state.copy(
                config = state.config.copy(
                    resize = state.config.resize.copy(
                        mode = ResizeMode.CUSTOM,
                        customWidth = width,
                        customHeight = height,
                    ),
                ),
            )
        }
    }

    fun updateMaintainAspectRatio(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(config = state.config.copy(resize = state.config.resize.copy(maintainAspectRatio = enabled)))
        }
        if (enabled) updateResizeFromCurrentSelectionIfNeeded()
    }

    fun updateAllowUpscale(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(config = state.config.copy(resize = state.config.resize.copy(allowUpscale = enabled)))
        }
    }

    fun updateJpegBackgroundColor(color: Int) {
        _uiState.update { state ->
            state.copy(config = state.config.copy(jpegBackgroundColor = color))
        }
    }

    fun updateKeepOriginal(enabled: Boolean) {
        _uiState.update { it.copy(keepOriginal = enabled) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clear()
        }
    }

    fun removeHistoryItem(id: String) {
        viewModelScope.launch {
            historyRepository.remove(id)
        }
    }

    fun openHistoryItem(id: String) {
        _uiState.update { state ->
            val item = state.history.firstOrNull { it.id == id } ?: return@update state
            state.copy(
                results = (listOf(item) + state.results).distinctBy { it.id },
                selectedResultId = item.id,
            )
        }
    }

    fun startCompression() {
        val images = _uiState.value.selectedImages
        startCompressionForImages(images, appendToResults = false)
    }

    fun retryFailedItems() {
        val state = _uiState.value
        val failedIds = state.batch.items.filter { it.status == BatchItemStatus.FAILED }.map { it.imageId }.toSet()
        val failedImages = state.selectedImages.filter { it.id in failedIds }
        if (failedImages.isNotEmpty()) {
            startCompressionForImages(failedImages, appendToResults = true)
        }
    }

    fun cancelCompression() {
        compressionJob?.cancel()
    }

    fun selectedResult(): ProcessedImage? {
        val state = _uiState.value
        return state.results.firstOrNull { it.id == state.selectedResultId } ?: state.results.firstOrNull()
    }

    fun selectResult(id: String) {
        _uiState.update { it.copy(selectedResultId = id) }
    }

    fun saveSelected(requestedName: String? = null) {
        val current = _uiState.value
        if (current.isSaving || current.pendingAdAction !is PendingAdAction.None) return
        val image = selectedResult() ?: return
        val requestId = UUID.randomUUID().toString()
        pendingSaveRequests[requestId] = PendingSaveRequest.Single(image, requestedName)
        _uiState.update {
            it.copy(
                pendingAdAction = PendingAdAction.SaveResult(requestId),
                message = null,
            )
        }
    }

    fun saveAllResults() {
        val current = _uiState.value
        if (current.isSaving || current.pendingAdAction !is PendingAdAction.None) return
        val results = current.results
        if (results.isEmpty()) return
        val requestId = UUID.randomUUID().toString()
        pendingSaveRequests[requestId] = PendingSaveRequest.Batch(results)
        _uiState.update {
            it.copy(
                pendingAdAction = PendingAdAction.SaveResult(requestId),
                message = null,
            )
        }
    }

    fun requestOpenHistory() {
        _uiState.update { state ->
            if (state.pendingAdAction !is PendingAdAction.None) {
                state
            } else {
                state.copy(pendingAdAction = PendingAdAction.OpenHistory)
            }
        }
    }

    fun performPendingSave(requestId: String) {
        val request = pendingSaveRequests.remove(requestId) ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    pendingAdAction = PendingAdAction.None,
                    isSaving = true,
                    message = null,
                )
            }
            val message = when (request) {
                is PendingSaveRequest.Single -> saveSingle(request.image, request.requestedName)
                is PendingSaveRequest.Batch -> saveBatch(request.images)
            }
            _uiState.update {
                it.copy(
                    isSaving = false,
                    message = message,
                )
            }
        }
    }

    fun consumePendingAdAction() {
        _uiState.update { it.copy(pendingAdAction = PendingAdAction.None) }
    }

    private suspend fun saveSingle(
        image: ProcessedImage,
        requestedName: String?,
    ): String {
        val saved = saveImages.saveOne(image, requestedName).getOrElse {
            return SAVE_FAILURE_MESSAGE
        }
        if (saved.uriString.isBlank()) return SAVE_FAILURE_MESSAGE

        val historyResult = historyRepository.recordSuccessfulOutput(
            output = image.asSavedHistoryOutput(saved),
            operationType = image.operationType,
        )
        return if (historyResult.isSuccess) {
            SAVE_SUCCESS_MESSAGE
        } else {
            HISTORY_FAILURE_MESSAGE
        }
    }

    private suspend fun saveBatch(images: List<ProcessedImage>): String {
        val savedResults = saveImages.saveAll(images)
        if (savedResults.size != images.size) return SAVE_FAILURE_MESSAGE

        var allSaved = true
        savedResults.zip(images).forEach { (savedResult, image) ->
            val saved = savedResult.getOrNull()
            if (saved?.uriString.isNullOrBlank()) {
                allSaved = false
            } else {
                val historyResult = historyRepository.recordSuccessfulOutput(
                    output = image.asSavedHistoryOutput(saved),
                    operationType = image.operationType,
                )
                if (historyResult.isFailure) allSaved = false
            }
        }
        return if (allSaved) SAVE_SUCCESS_MESSAGE else SAVE_FAILURE_MESSAGE
    }

    fun removeBackground() {
        if (backgroundJob?.isActive == true) return
        val image = _uiState.value.selectedImages.singleOrNull()
        if (image == null) {
            showMessage("Select one image before removing a background.")
            return
        }
        backgroundJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    backgroundState = BackgroundUiState.Running(
                        progress = 0f,
                        stage = BackgroundProcessingStage.PREPARING_IMAGE,
                    ),
                )
            }
            val result = try {
                removeBackgroundUseCase(ImageSource(image.id, image.uriString)) { progress ->
                    _uiState.update { state ->
                        val safeProgress = progress.coerceIn(0f, 1f)
                        state.copy(
                            backgroundState = BackgroundUiState.Running(
                                progress = safeProgress,
                                stage = backgroundStageForProgress(safeProgress),
                            ),
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                _uiState.update { it.copy(backgroundState = BackgroundUiState.Cancelled) }
                return@launch
            } catch (error: Throwable) {
                BackgroundRemovalResult.Failure(error.message ?: "Background removal failed.")
            }
            if (result is BackgroundRemovalResult.Success) {
                recordSuccessfulOutput(result.image, HistoryOperationType.BACKGROUND_REMOVED)
            }
            _uiState.update { state ->
                state.copy(
                    backgroundState = when (result) {
                        is BackgroundRemovalResult.Success -> BackgroundUiState.Success(result.image)
                        is BackgroundRemovalResult.Unavailable -> BackgroundUiState.Unavailable(result.reason)
                        is BackgroundRemovalResult.Failure -> BackgroundUiState.Error(result.message)
                    },
                )
            }
        }
    }

    fun cancelBackgroundRemoval() {
        backgroundJob?.cancel()
        _uiState.update { it.copy(backgroundState = BackgroundUiState.Cancelled) }
    }

    fun replaceBackground(config: BackgroundReplacementConfig) {
        val image = (_uiState.value.backgroundState as? BackgroundUiState.Success)?.image ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(backgroundReplaceProgress = 0f) }
            replaceBackgroundUseCase(image, config) { progress ->
                _uiState.update { it.copy(backgroundReplaceProgress = progress.coerceIn(0f, 1f)) }
            }.onSuccess { processed ->
                recordSuccessfulOutput(processed, HistoryOperationType.BACKGROUND_REMOVED)
                _uiState.update { state ->
                    state.copy(
                        results = listOf(processed) + state.results.filterNot { it.id == processed.id },
                        selectedResultId = processed.id,
                        backgroundReplaceProgress = null,
                        message = "Background exported.",
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        backgroundReplaceProgress = null,
                        message = error.message ?: "Could not replace the background.",
                    )
                }
            }
        }
    }

    fun consumeMessage() {
        _uiState.update { it.copy(message = null) }
    }

    private fun startCompressionForImages(images: List<ImageInfo>, appendToResults: Boolean) {
        if (images.isEmpty() || compressionJob?.isActive == true) return
        val config = _uiState.value.config
        val targetValidation = TargetSizeValidator.validate(config.targetSize)
        if (!targetValidation.isValid) {
            showMessage(targetValidation.message ?: "Check the target size.")
            return
        }
        val invalidResize = images.firstNotNullOfOrNull { image ->
            ResizeCalculator.calculate(image.width, image.height, config.resize)
                .takeUnless { it.validation.isValid }
                ?.validation
                ?.message
        }
        if (invalidResize != null) {
            showMessage(invalidResize)
            return
        }

        val initialItems = images.map {
            BatchItemUiState(
                imageId = it.id,
                name = it.displayName,
                status = BatchItemStatus.QUEUED,
            )
        }
        _uiState.update { state ->
            state.copy(
                batch = BatchUiState(isRunning = true, items = initialItems),
                results = if (appendToResults) state.results else emptyList(),
                selectedResultId = if (appendToResults) state.selectedResultId else null,
                message = null,
            )
        }

        compressionJob = viewModelScope.launch {
            val newResults = mutableListOf<ProcessedImage>()
            var wasCancelled = false
            try {
                images.forEachIndexed { index, image ->
                    var lastProgress = -1f
                    var lastStage = ProcessingStage.READING_IMAGE
                    updateBatchItem(image.id) {
                        it.copy(
                            status = BatchItemStatus.RUNNING,
                            progress = 0f,
                            stage = ProcessingStage.READING_IMAGE,
                            error = null,
                        )
                    }
                    compressImage(image, config) { progress ->
                        val safeProgress = progress.coerceIn(0f, 1f)
                        val stage = stageForProgress(safeProgress)
                        if (safeProgress - lastProgress >= 0.02f || stage != lastStage || safeProgress >= 1f) {
                            lastProgress = safeProgress
                            lastStage = stage
                            updateBatchItem(image.id) {
                                it.copy(
                                    status = BatchItemStatus.RUNNING,
                                    progress = safeProgress,
                                    stage = stage,
                                )
                            }
                        }
                    }.onSuccess { result ->
                        newResults += result
                        updateBatchItem(image.id) {
                            it.copy(
                                status = BatchItemStatus.SUCCESS,
                                progress = 1f,
                                stage = ProcessingStage.COMPLETE,
                                result = result,
                            )
                        }
                    }.onFailure { error ->
                        if (error is CancellationException) throw error
                        updateBatchItem(image.id) {
                            it.copy(
                                status = BatchItemStatus.FAILED,
                                progress = 0f,
                                error = error.message ?: "Compression failed.",
                            )
                        }
                    }
                    _uiState.update { state ->
                        state.copy(batch = state.batch.copy(currentIndex = index + 1))
                    }
                }
            } catch (cancelled: CancellationException) {
                wasCancelled = true
                _uiState.update { state ->
                    state.copy(
                        batch = state.batch.copy(
                            items = state.batch.items.map { item ->
                                if (item.status == BatchItemStatus.QUEUED || item.status == BatchItemStatus.RUNNING) {
                                    item.copy(status = BatchItemStatus.CANCELLED, error = "Cancelled")
                                } else {
                                    item
                                }
                            },
                        ),
                    )
                }
            } finally {
                val finalState = _uiState.value
                val successes = finalState.batch.items.count { it.status == BatchItemStatus.SUCCESS }
                val failures = finalState.batch.items.count { it.status == BatchItemStatus.FAILED }
                val summary = ProcessingSummary(
                    total = finalState.batch.items.size,
                    successful = successes,
                    failed = failures,
                    cancelled = wasCancelled,
                )
                val combinedResults = if (appendToResults) {
                    (_uiState.value.results + newResults).distinctBy { it.id }
                } else {
                    newResults
                }
                recordSuccessfulOutputs(newResults)
                _uiState.update { state ->
                    state.copy(
                        batch = state.batch.copy(
                            isRunning = false,
                            summary = summary,
                        ),
                        results = combinedResults,
                        selectedResultId = combinedResults.firstOrNull()?.id,
                    )
                }
            }
        }
    }

    private fun updateBatchItem(id: String, transform: (BatchItemUiState) -> BatchItemUiState) {
        _uiState.update { state ->
            state.copy(
                batch = state.batch.copy(
                    items = state.batch.items.map { item ->
                        if (item.imageId == id) transform(item) else item
                    },
                ),
            )
        }
    }

    private fun updateResizeFromCurrentSelectionIfNeeded() {
        _uiState.update { state ->
            val first = state.selectedImages.firstOrNull() ?: return@update state
            val resize = state.config.resize
            if (resize.mode != ResizeMode.CUSTOM || !resize.maintainAspectRatio) return@update state
            val width = resize.customWidth.toIntOrNull()
            val height = resize.customHeight.toIntOrNull()
            val updated = when {
                width != null -> resize.copy(customHeight = ResizeCalculator.aspectHeight(width, first.width, first.height).toString())
                height != null -> resize.copy(customWidth = ResizeCalculator.aspectWidth(height, first.width, first.height).toString())
                else -> resize.copy(customWidth = first.width.toString(), customHeight = first.height.toString())
            }
            state.copy(config = state.config.copy(resize = updated))
        }
    }

    private fun showMessage(message: String) {
        _uiState.update { it.copy(message = message) }
    }

    private suspend fun recordSuccessfulOutput(
        output: ProcessedImage,
        operationType: HistoryOperationType,
    ) {
        historyRepository.recordSuccessfulOutput(output, operationType)
    }

    private fun recordSuccessfulOutputs(outputs: List<ProcessedImage>) {
        if (outputs.isEmpty()) return
        viewModelScope.launch {
            outputs.forEach { output ->
                recordSuccessfulOutput(output, output.operationType)
            }
        }
    }

    private fun stageForProgress(progress: Float): ProcessingStage {
        return when {
            progress < 0.18f -> ProcessingStage.READING_IMAGE
            progress < 0.35f -> ProcessingStage.OPTIMIZING_RESOLUTION
            progress < 0.92f -> ProcessingStage.COMPRESSING
            progress < 1f -> ProcessingStage.VALIDATING_RESULT
            else -> ProcessingStage.COMPLETE
        }
    }

    private fun backgroundStageForProgress(progress: Float): BackgroundProcessingStage {
        return when {
            progress < 0.2f -> BackgroundProcessingStage.PREPARING_IMAGE
            progress < 0.72f -> BackgroundProcessingStage.REMOVING_BACKGROUND
            progress < 0.86f -> BackgroundProcessingStage.REFINING_EDGES
            progress < 1f -> BackgroundProcessingStage.FINALIZING
            else -> BackgroundProcessingStage.COMPLETE
        }
    }
}

data class PhotoCompressorUiState(
    val selectedImages: List<ImageInfo> = emptyList(),
    val isLoadingSelection: Boolean = false,
    val selectionError: String? = null,
    val config: CompressionConfig = CompressionConfig(),
    val batch: BatchUiState = BatchUiState(),
    val results: List<ProcessedImage> = emptyList(),
    val selectedResultId: String? = null,
    val history: List<ProcessedImage> = emptyList(),
    val keepOriginal: Boolean = true,
    val isSaving: Boolean = false,
    val backgroundState: BackgroundUiState = BackgroundUiState.Idle,
    val backgroundReplaceProgress: Float? = null,
    val pendingAdAction: PendingAdAction = PendingAdAction.None,
    val message: String? = null,
) {
    val targetValidation: ValidationResult = TargetSizeValidator.validate(config.targetSize)

    val totalProgress: Float = if (batch.items.isEmpty()) {
        0f
    } else {
        batch.items.sumOf { it.progress.toDouble() }.toFloat() / batch.items.size.toFloat()
    }

    val currentOutputDimension: Dimension? = selectedImages.firstOrNull()?.let {
        ResizeCalculator.calculate(it.width, it.height, config.resize).dimension
    }

    val resizeValidation: ValidationResult = selectedImages.firstOrNull()
        ?.let { ResizeCalculator.calculate(it.width, it.height, config.resize).validation }
        ?: ValidationResult(true)

    val alphaToJpegWarning: String? =
        if (config.outputFormat == ImageFormat.JPEG && selectedImages.any { it.hasAlpha }) {
            "Transparent pixels will be placed over the selected background color for JPG export."
        } else {
            null
        }

    val hasActiveProcessing: Boolean =
        batch.isRunning ||
            backgroundState is BackgroundUiState.Running ||
            backgroundReplaceProgress != null ||
            isSaving
}

sealed interface PendingAdAction {
    data object OpenHistory : PendingAdAction

    data class SaveResult(
        val requestId: String,
    ) : PendingAdAction

    data object None : PendingAdAction
}

private sealed interface PendingSaveRequest {
    data class Single(
        val image: ProcessedImage,
        val requestedName: String?,
    ) : PendingSaveRequest

    data class Batch(
        val images: List<ProcessedImage>,
    ) : PendingSaveRequest
}

data class BatchUiState(
    val isRunning: Boolean = false,
    val items: List<BatchItemUiState> = emptyList(),
    val currentIndex: Int = 0,
    val summary: ProcessingSummary? = null,
)

data class BatchItemUiState(
    val imageId: String,
    val name: String,
    val status: BatchItemStatus,
    val progress: Float = 0f,
    val stage: ProcessingStage = ProcessingStage.READING_IMAGE,
    val result: ProcessedImage? = null,
    val error: String? = null,
)

enum class BatchItemStatus {
    QUEUED,
    RUNNING,
    SUCCESS,
    FAILED,
    CANCELLED,
}

enum class ProcessingStage(val label: String) {
    READING_IMAGE("Reading image"),
    OPTIMIZING_RESOLUTION("Optimizing resolution"),
    COMPRESSING("Compressing"),
    VALIDATING_RESULT("Validating result"),
    COMPLETE("Complete"),
}

sealed interface BackgroundUiState {
    data object Idle : BackgroundUiState
    data class Running(
        val progress: Float,
        val stage: BackgroundProcessingStage,
    ) : BackgroundUiState
    data class Success(val image: ProcessedImage) : BackgroundUiState
    data class Unavailable(val reason: String) : BackgroundUiState
    data class Error(val message: String) : BackgroundUiState
    data object Cancelled : BackgroundUiState
}

enum class BackgroundProcessingStage(val label: String) {
    PREPARING_IMAGE("Preparing image"),
    REMOVING_BACKGROUND("Removing background"),
    REFINING_EDGES("Refining edges"),
    FINALIZING("Finalizing"),
    COMPLETE("Complete"),
}

fun ResizeMode.percentLabel(): String {
    return when (this) {
        ResizeMode.ORIGINAL -> "Original"
        ResizeMode.PERCENT_25 -> "25%"
        ResizeMode.PERCENT_50 -> "50%"
        ResizeMode.PERCENT_75 -> "75%"
        ResizeMode.CUSTOM -> "Custom"
    }
}

fun Float.percentText(): String = "${(this * 100f).roundToInt()}%"

private fun ProcessedImage.asSavedHistoryOutput(saved: SavedImage): ProcessedImage {
    return copy(
        filePath = saved.uriString,
        displayName = saved.displayName,
        savedUriString = saved.uriString,
        createdTimestamp = System.currentTimeMillis(),
    )
}

private const val SAVE_SUCCESS_MESSAGE = "Image saved successfully"
private const val SAVE_FAILURE_MESSAGE = "Couldn't save image. Please try again."
private const val HISTORY_FAILURE_MESSAGE = "Image saved, but couldn't update History."
