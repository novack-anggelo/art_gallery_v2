package com.novack.artgalleryv2.di

import android.content.Context
import com.novack.artgalleryv2.core.artwork.domain.repository.ArtworkRepository
import com.novack.artgalleryv2.feature.discover.domain.repository.DiscoverPreferencesRepository
import io.mockk.mockk
import org.junit.Test
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication
import kotlin.test.assertNotNull

class AppModulesTest {

    @Test
    fun `application graph resolves repositories`() {
        val application = koinApplication {
            androidContext(mockk<Context>(relaxed = true))
            modules(appModules)
        }

        try {
            val repository =
                application.koin.get<ArtworkRepository>()
            val preferencesRepository =
                application.koin.get<DiscoverPreferencesRepository>()

            assertNotNull(repository)
            assertNotNull(preferencesRepository)
        } finally {
            application.close()
        }
    }
}
