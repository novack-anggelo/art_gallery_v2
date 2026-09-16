package com.novack.artgalleryv2.core.network.di

import kotlinx.serialization.json.Json
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Test
import org.koin.dsl.koinApplication
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class NetworkModuleTest {
    @Test
    fun `network module constructs shared dependencies`() {
        val application = koinApplication {
            modules(networkModule(isDebug = true))
        }

        try {
            assertNotNull(application.koin.get<Json>())
            assertEquals(
                HttpLoggingInterceptor.Level.BASIC,
                application.koin.get<HttpLoggingInterceptor>().level,
            )
        } finally {
            application.close()
        }
    }

    @Test
    fun `release network module disables logging`() {
        val application = koinApplication {
            modules(networkModule(isDebug = false))
        }

        try {
            assertEquals(
                HttpLoggingInterceptor.Level.NONE,
                application.koin.get<HttpLoggingInterceptor>().level,
            )
        } finally {
            application.close()
        }
    }
}
