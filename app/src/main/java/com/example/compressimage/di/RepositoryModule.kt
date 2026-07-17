package com.example.compressimage.di

import com.example.compressimage.backgroundremoval.OnDeviceBackgroundRemovalRepository
import com.example.compressimage.backgroundremoval.OnnxBackgroundRemovalEngine
import com.example.compressimage.backgroundremoval.BackgroundRemovalEngine
import com.example.compressimage.data.repository.AndroidImageRepository
import com.example.compressimage.data.storage.DataStoreHistoryRepository
import com.example.compressimage.domain.repository.BackgroundRemovalRepository
import com.example.compressimage.domain.repository.HistoryRepository
import com.example.compressimage.domain.repository.ImageRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindImageRepository(repository: AndroidImageRepository): ImageRepository

    @Binds
    @Singleton
    abstract fun bindBackgroundRemovalEngine(engine: OnnxBackgroundRemovalEngine): BackgroundRemovalEngine

    @Binds
    @Singleton
    abstract fun bindBackgroundRemovalRepository(
        repository: OnDeviceBackgroundRemovalRepository,
    ): BackgroundRemovalRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(repository: DataStoreHistoryRepository): HistoryRepository
}
