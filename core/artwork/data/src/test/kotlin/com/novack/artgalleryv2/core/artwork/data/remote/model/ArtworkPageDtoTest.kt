package com.novack.artgalleryv2.core.artwork.data.remote.model

import com.novack.artgalleryv2.core.artwork.data.remote.model.ArtworkPageDto
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ArtworkPageDtoTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `artwork page response is deserialized`() {
        val fixture = checkNotNull(
            javaClass.classLoader
                ?.getResource("fixtures/artworks_page.json")
                ?.readText()
        )

        val result = json.decodeFromString<ArtworkPageDto>(fixture)

        assertEquals(1, result.pagination.currentPage)
        assertEquals(50, result.pagination.totalPages)
        assertEquals(
            "https://www.artic.edu/iiif/2",
            result.config.iiifUrl,
        )

        val artwork = result.data.first()

        assertEquals(27992, artwork.id)
        assertEquals(
            "A Sunday on La Grande Jatte — 1884",
            artwork.title,
        )
        assertEquals("Georges Seurat", artwork.artistTitle)
        assertEquals("Oil on canvas", artwork.mediumDisplay)
        assertEquals(true, artwork.isPublicDomain)
        assertEquals(843, artwork.thumbnail?.width)
    }

    @Test
    fun `missing optional artwork metadata becomes null`() {
        val fixture = checkNotNull(
            javaClass.classLoader
                ?.getResource("fixtures/artworks_page.json")
                ?.readText()
        )

        val result = json.decodeFromString<ArtworkPageDto>(fixture)
        val artwork = result.data[1]

        assertNull(artwork.artistTitle)
        assertNull(artwork.dateDisplay)
        assertNull(artwork.mediumDisplay)
        assertNull(artwork.imageId)
        assertNull(artwork.thumbnail)
        assertNull(artwork.isPublicDomain)
    }
}
