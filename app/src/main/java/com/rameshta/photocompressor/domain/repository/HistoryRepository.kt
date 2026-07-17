package com.rameshta.photocompressor.domain.repository

import com.rameshta.photocompressor.domain.model.HistoryOperationType
import com.rameshta.photocompressor.domain.model.ProcessedImage
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    val history: Flow<List<ProcessedImage>>

    suspend fun recordSuccessfulOutput(
        output: ProcessedImage,
        operationType: HistoryOperationType = output.operationType,
    ): Result<Unit>

    suspend fun remove(id: String)

    suspend fun clear()
}
