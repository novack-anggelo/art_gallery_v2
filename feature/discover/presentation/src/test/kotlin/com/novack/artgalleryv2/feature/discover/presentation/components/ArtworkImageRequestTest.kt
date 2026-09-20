package com.novack.artgalleryv2.feature.discover.presentation.components

import com.novack.artgalleryv2.core.artwork.domain.model.ArtworkImage
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.Test

class ArtworkImageRequestTest {
    @Test
    fun `source website is sent as image request referer`() {
        val image = ArtworkImage(
            url = "https://www.artic.edu/iiif/2/image/full/843,/0/default.jpg",
            altText = null,
            aspectRatio = null,
            sourceWebsiteUrl = "https://www.artic.edu",
        )

        assertEquals(
            "https://www.artic.edu",
            image.networkHeaders()["Referer"],
        )
    }

    @Test
    fun `missing source website does not add a referer`() {
        val image = ArtworkImage(
            url = "https://example.com/image.jpg",
            altText = null,
            aspectRatio = null,
        )

        assertNull(image.networkHeaders()["Referer"])
    }
}
