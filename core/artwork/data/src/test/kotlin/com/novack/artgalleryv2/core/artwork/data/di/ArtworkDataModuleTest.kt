package com.novack.artgalleryv2.core.artwork.data.di

import com.novack.artgalleryv2.core.artwork.data.remote.api.ArtInstituteApi
import com.novack.artgalleryv2.core.network.di.networkModule
import org.junit.Test
import org.koin.dsl.koinApplication
import retrofit2.Retrofit
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ArtworkDataModuleTest {
    @Test
    fun `artwork data module constructs its dependencies`() {
        val application = koinApplication {
            modules(networkModule(isDebug = false), artworkDataModule)
        }

        try {
            val retrofit = application.koin.get<Retrofit>()
            val api = application.koin.get<ArtInstituteApi>()

            assertEquals(
                "https://api.artic.edu/api/v1/",
                retrofit.baseUrl().toString(),
            )
            assertNotNull(api)
        } finally {
            application.close()
        }
    }
}
