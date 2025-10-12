package com.project.readingstats.di

import com.project.readingstats.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Qualifier @Retention(AnnotationRetention.BINARY)
annotation class BooksApiKey

@Module
@InstallIn(SingletonComponent::class)
object KeysModule {
    @Provides @BooksApiKey
    fun provideBooksApiKey(): String = BuildConfig.GOOGLE_BOOKS_API_KEY
}