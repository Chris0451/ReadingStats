package com.project.readingstats.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton
import com.project.readingstats.features.catalog.data.GoogleBooksApi
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import okhttp3.logging.HttpLoggingInterceptor

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton
    fun provideOkHttp(@BooksApiKey key: String): OkHttpClient{
        val addKey = Interceptor { chain ->
            val req = chain.request()
            val url = req.url.newBuilder().addQueryParameter("key", key).build()
            chain.proceed(req.newBuilder().url(url).build())
        }
        val log = HttpLoggingInterceptor().apply {level = HttpLoggingInterceptor.Level.BASIC}
        return OkHttpClient.Builder().addInterceptor(addKey).build()
    }

    @Provides @Singleton
    fun provideMoshi(): Moshi =
        Moshi.Builder().build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/books/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()

    @Provides @Singleton
    fun provideBooksApi(retrofit: Retrofit): GoogleBooksApi =
        retrofit.create(GoogleBooksApi::class.java)
}