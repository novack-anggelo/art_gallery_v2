package com.novack.artgalleryv2.core.network.di

import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinApplication
import org.koin.test.verify.verify
import retrofit2.Retrofit
import kotlin.test.assertEquals

@OptIn(KoinExperimentalAPI::class)
class NetworkModuleTest {

    @Test
    fun `network module has a complete dependency graph`() {
        networkModule.verify()
    }

    @Test
    fun `network module constructs Retrofit with the expected base URL`() {
        val application = koinApplication {
            modules(networkModule)
        }

        try {
            val retrofit = application.koin.get<Retrofit>()

            assertEquals(
                "https://api.artic.edu/api/v1/",
                retrofit.baseUrl().toString(),
            )
        } finally {
            application.close()
        }
    }
}