package com.project.readingstats.di

import com.project.readingstats.features.home.data.repository.HomeRepositoryImpl
import com.project.readingstats.features.home.domain.repository.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {
    @Binds @Singleton
    abstract fun bindHomeRepository(impl: HomeRepositoryImpl): HomeRepository
}