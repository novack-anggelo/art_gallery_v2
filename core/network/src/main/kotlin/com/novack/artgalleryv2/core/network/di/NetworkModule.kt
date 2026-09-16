package com.novack.artgalleryv2.core.network.di

import kotlinx.serialization.json.Json
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun networkModule(isDebug: Boolean) = module {
    singleOf(::provideJson)
    single { provideHttpLoggingInterceptor(isDebug) }
}

internal fun provideJson(): Json = Json {
    ignoreUnknownKeys = true
}

internal fun provideHttpLoggingInterceptor(
    isDebug: Boolean,
): HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
    level = if (isDebug) {
        HttpLoggingInterceptor.Level.BASIC
    } else {
        HttpLoggingInterceptor.Level.NONE
    }
}
