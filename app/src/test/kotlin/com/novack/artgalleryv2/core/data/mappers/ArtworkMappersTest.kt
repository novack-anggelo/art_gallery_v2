package com.novack.artgalleryv2.core.data.mappers

import com.novack.artgalleryv2.core.data.remote.model.ApiConfigDto
import com.novack.artgalleryv2.core.data.remote.model.ArtworkSummaryDto
import com.novack.artgalleryv2.core.data.remote.model.ArtworkThumbnailDto
import com.novack.artgalleryv2.core.data.remote.model.PaginatedResponseDto
import com.novack.artgalleryv2.core.data.remote.model.PaginationDto
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ArtworkMappersTest {

    @Test
    fun `page maps artwork and constructs IIIF image`() {
        val response = PaginatedResponseDto(
            pagination = PaginationDto(
                total = 1,
                limit = 20,
                currentPage = 1,
                totalPages = 1,
            ),
            data = listOf(
                ArtworkSummaryDto(
                    id = 27992,
                    title = "A Sunday on La Grande Jatte — 1884",
                    artistTitle = "Georges Seurat",
                    dateDisplay = "1884–86",
                    mediumDisplay = "Oil on canvas",
                    imageId =
                        "2d484387-2509-5e8e-2c43-22f9981972eb",
                    isPublicDomain = true,
                    thumbnail = ArtworkThumbnailDto(
                        width = 843,
                        height = 563,
                        altText = "People beside the water.",
                    ),
                ),
            ),
            config = ApiConfigDto(
                iiifUrl = "https://www.artic.edu/iiif/2/",
            ),
        )

        val result = response.toDomain()
        val artwork = result.single()

        assertEquals(27992, artwork.id)
        assertEquals("Georges Seurat", artwork.artist)
        assertEquals("1884–86", artwork.dateDisplay)
        assertEquals("Oil on canvas", artwork.mediumDisplay)
        assertTrue(artwork.isPublicDomain)
        assertEquals(
            "https://www.artic.edu/iiif/2/" +
                    "2d484387-2509-5e8e-2c43-22f9981972eb/" +
                    "full/843,/0/default.jpg",
            artwork.image.url,
        )
        assertEquals(
            843f / 563f,
            artwork.image.aspectRatio,
        )
    }

    @Test
    fun `artwork without image is excluded`() {
        val dto = ArtworkSummaryDto(
            id = 42,
            title = "Untitled",
            artistTitle = " ",
            dateDisplay = null,
            mediumDisplay = " ",
            imageId = null,
            isPublicDomain = null,
            thumbnail = null,
        )

        val result = dto.toDomainOrNull(
            iiifBaseUrl = "https://www.artic.edu/iiif/2",
        )

        assertNull(result)
    }

    @Test
    fun `optional metadata maps to conservative domain values`() {
        val dto = ArtworkSummaryDto(
            id = 42,
            title = "Untitled",
            artistTitle = " ",
            dateDisplay = null,
            mediumDisplay = " ",
            imageId = "valid-image-id",
            isPublicDomain = null,
            thumbnail = null,
        )

        val result = checkNotNull(
            dto.toDomainOrNull(
                iiifBaseUrl = "https://www.artic.edu/iiif/2",
            )
        )

        assertNull(result.artist)
        assertNull(result.dateDisplay)
        assertNull(result.mediumDisplay)
        assertFalse(result.isPublicDomain)
        assertNull(result.image.altText)
        assertNull(result.image.aspectRatio)
    }
}
