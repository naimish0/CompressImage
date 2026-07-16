package com.example.compressimage.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.compressimage.ads.AdFrequencyCapper
import com.example.compressimage.domain.model.BackgroundRemovalResult
import com.example.compressimage.domain.model.BackgroundReplacementConfig
import com.example.compressimage.domain.model.CompressionConfig
import com.example.compressimage.domain.model.CompressionMode
import com.example.compressimage.domain.model.Dimension
import com.example.compressimage.domain.model.ImageFormat
import com.example.compressimage.domain.model.ImageInfo
import com.example.compressimage.domain.model.ImageSource
import com.example.compressimage.domain.model.ProcessedImage
import com.example.compressimage.domain.model.ProcessingSummary
import com.example.compressimage.domain.model.ResizeConfig
import com.example.compressimage.domain.model.ResizeMode
import com.example.compressimage.domain.model.SavedImage
import com.example.compressimage.domain.model.TargetSizePreset
import com.example.compressimage.domain.model.TargetSizeUnit
import com.example.compressimage.domain.usecase.CompressImageUseCase
import com.example.compressimage.domain.usecase.LoadImageInfoUseCase
import com.example.compressimage.domain.usecase.RemoveBackgroundUseCase
import com.example.compressimage.domain.usecase.ReplaceBackgroundUseCase
import com.example.compressimage.domain.usecase.SaveImagesUseCase
import com.example.compressimage.util.ResizeCalculator
import com.example.compressimage.util.TargetSizeValidator
import com.example.compressimage.util.ValidationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class PhotoCompressorViewModel @Inject constructor(
    private val loadImageInfo: LoadImageInfoUseCase,
    private val compressImage: CompressImageUseCase,
    private val saveImages: SaveImagesUseCase,
    private val removeBackgroundUseCase: RemoveBackgroundUseCase,
    private val replaceBackgroundUseCase: ReplaceBackgroundUseCase,
    private val adFrequencyCapper: AdFrequencyCapper,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PhotoCompressorUiState())
    val uiState: StateFlow<PhotoCompressorUiState> = _uiState.asStateFlow()

    private var compressionJob: Job? = null
    private var backgroundJob: Job? = null

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
        _uiState.update { it.copy(history = emptyList()) }
    }

    fun removeHistoryItem(id: String) {
        _uiState.update { state -> state.copy(history = state.history.filterNot { it.id == id }) }
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
        val image = selectedResult() ?: return
        viewModelScope.launch {
            saveImages.saveOne(image, requestedName)
                .onSuccess { saved -> showSavedMessage(saved) }
                .onFailure { error -> showMessage(error.message ?: "Save failed.") }
        }
    }

    fun saveAllResults() {
        val results = _uiState.value.results
        if (results.isEmpty()) return
        viewModelScope.launch {
            val saved = saveImages.saveAll(results)
            val successful = saved.count { it.isSuccess }
            val failed = saved.size - successful
            showMessage(
                if (failed == 0) {
                    "Saved $successful image${if (successful == 1) "" else "s"} to Pictures/Photo Compressor."
                } else {
                    "Saved $successful image${if (successful == 1) "" else "s"}; $failed failed."
                },
            )
        }
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

    fun shouldShowInterstitialBeforeResult(): Boolean {
        val shouldShow = _uiState.value.showInterstitialBeforeResult
        if (shouldShow) {
            _uiState.update { it.copy(showInterstitialBeforeResult = false) }
        }
        return shouldShow
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
                _uiState.update { state ->
                    state.copy(
                        batch = state.batch.copy(
                            isRunning = false,
                            summary = summary,
                        ),
                        results = combinedResults,
                        selectedResultId = combinedResults.firstOrNull()?.id,
                        history = (combinedResults + state.history).distinctBy { it.filePath }.take(30),
                        showInterstitialBeforeResult = successes > 0 &&
                            !wasCancelled &&
                            adFrequencyCapper.recordCompletedOperation(),
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

    private fun showSavedMessage(saved: SavedImage) {
        showMessage("Saved ${saved.displayName} to Pictures/Photo Compressor.")
    }

    private fun showMessage(message: String) {
        _uiState.update { it.copy(message = message) }
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
    val backgroundState: BackgroundUiState = BackgroundUiState.Idle,
    val backgroundReplaceProgress: Float? = null,
    val showInterstitialBeforeResult: Boolean = false,
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
