package com.rameshta.photocompressor.di

import com.rameshta.photocompressor.backgroundremoval.OnDeviceBackgroundRemovalRepository
import com.rameshta.photocompressor.backgroundremoval.OnnxBackgroundRemovalEngine
import com.rameshta.photocompressor.backgroundremoval.BackgroundRemovalEngine
import com.rameshta.photocompressor.data.repository.AndroidImageRepository
import com.rameshta.photocompressor.data.storage.DataStoreHistoryRepository
import com.rameshta.photocompressor.domain.repository.BackgroundRemovalRepository
import com.rameshta.photocompressor.domain.repository.HistoryRepository
import com.rameshta.photocompressor.domain.repository.ImageRepository
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
