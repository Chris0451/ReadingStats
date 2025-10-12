package com.project.readingstats.di

import com.project.readingstats.features.shelves.data.repository.ShelvesRepositoryImpl
import com.project.readingstats.features.shelves.domain.repository.ShelvesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ShelvesModule {
    @Binds @Singleton
    abstract fun bindShelvesRepository(impl: ShelvesRepositoryImpl): ShelvesRepository
}