package com.novack.artgalleryv2.di

import com.novack.artgalleryv2.core.domain.repository.ArtworkRepository
import org.junit.Test
import org.koin.dsl.koinApplication
import kotlin.test.assertNotNull

class AppModulesTest {

    @Test
    fun `application graph resolves artwork repository`() {
        val application = koinApplication {
            modules(appModules)
        }

        try {
            val repository =
                application.koin.get<ArtworkRepository>()

            assertNotNull(repository)
        } finally {
            application.close()
        }
    }
}
