package com.project.readingstats.di

import com.google.firebase.firestore.FirebaseFirestore
import com.project.readingstats.features.catalog.data.repository.UserPreferencesRepositoryImpl
import com.project.readingstats.features.catalog.domain.repository.UserPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
@Module
@InstallIn(SingletonComponent::class)
object UserPreferencesModule {
    @Provides @Singleton
    fun provideUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository = impl
}