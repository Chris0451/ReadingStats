package com.project.readingstats.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.project.readingstats.features.auth.data.repository.AuthRepositoryImpl
import com.project.readingstats.features.auth.data.source.FirebaseAuthDataSource
import com.project.readingstats.features.auth.data.source.FirestoreUserDataSource
import com.project.readingstats.features.auth.domain.repository.AuthRepository
import com.project.readingstats.features.auth.domain.usecase.CheckUsernameAvailableUseCase
import com.project.readingstats.features.auth.domain.usecase.RegisterUserUseCase
import dagger.Provides
import javax.inject.Singleton

object AuthModule {
    @Provides @Singleton fun provideAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    @Provides @Singleton fun provideDb(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides @Singleton fun provideAuthDs(auth: FirebaseAuth) = FirebaseAuthDataSource(auth)

    @Provides @Singleton
    fun provideUserDs(db: FirebaseFirestore) = FirestoreUserDataSource(db)

    @Provides @Singleton
    fun provideAuthRepo(
        authDs: FirebaseAuthDataSource,
        userDs: FirestoreUserDataSource
    ): AuthRepository = AuthRepositoryImpl(authDs, userDs)

    @Provides @Singleton
    fun provideCheckUsername(repository: AuthRepository) = CheckUsernameAvailableUseCase(repository)

    @Provides @Singleton
    fun provideRegister(repository: AuthRepository) = RegisterUserUseCase(repository)
}