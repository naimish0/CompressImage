package com.example.compressimage.domain.repository

import com.example.compressimage.domain.model.HistoryOperationType
import com.example.compressimage.domain.model.ProcessedImage
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
