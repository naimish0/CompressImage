package com.example.compressimage.di

import com.example.compressimage.backgroundremoval.OfflineBackgroundRemovalRepository
import com.example.compressimage.data.repository.AndroidImageRepository
import com.example.compressimage.domain.repository.BackgroundRemovalRepository
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
    abstract fun bindBackgroundRemovalRepository(
        repository: OfflineBackgroundRemovalRepository,
    ): BackgroundRemovalRepository
}
