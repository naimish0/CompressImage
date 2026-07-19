package com.rameshta.photocompressor.ui.history

import com.rameshta.photocompressor.domain.model.ProcessedImage
import com.rameshta.photocompressor.ui.UiText

typealias HistoryItem = ProcessedImage

sealed interface HistoryUiState {
    data object Loading : HistoryUiState
    data object Empty : HistoryUiState
    data class Content(val items: List<HistoryItem>) : HistoryUiState
    data class Error(val message: UiText) : HistoryUiState
}
