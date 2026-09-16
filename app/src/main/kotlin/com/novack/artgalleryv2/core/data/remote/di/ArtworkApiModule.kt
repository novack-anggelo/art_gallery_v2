package com.novack.artgalleryv2.core.data.remote.di

import com.novack.artgalleryv2.core.data.remote.api.ArtInstituteApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

private const val ART_INSTITUTE_BASE_URL = "https://api.artic.edu/api/v1/"
private const val AIC_USER_AGENT_HEADER = "AIC-User-Agent"
private const val AIC_USER_AGENT = "ArtGalleryAndroid/1.0"
private val jsonMediaType = "application/json".toMediaType()

val artworkApiModule = module {
    singleOf(::provideOkHttpClient)
    singleOf(::provideRetrofit)
    singleOf(::provideArtInstituteApi)
}

internal fun provideOkHttpClient(
    loggingInterceptor: HttpLoggingInterceptor,
): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request()
            .newBuilder()
            .header(AIC_USER_AGENT_HEADER, AIC_USER_AGENT)
            .build()
        chain.proceed(request)
    }
    .addInterceptor(loggingInterceptor)
    .build()

internal fun provideRetrofit(
    json: Json,
    okHttpClient: OkHttpClient,
): Retrofit = Retrofit.Builder()
    .baseUrl(ART_INSTITUTE_BASE_URL)
    .client(okHttpClient)
    .addConverterFactory(json.asConverterFactory(jsonMediaType))
    .build()

internal fun provideArtInstituteApi(
    retrofit: Retrofit,
): ArtInstituteApi = retrofit.create(ArtInstituteApi::class.java)
