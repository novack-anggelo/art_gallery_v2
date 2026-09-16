package com.novack.artgalleryv2.core.artwork.data.repository

import androidx.paging.testing.asSnapshot
import com.novack.artgalleryv2.core.artwork.data.fake.FakeArtInstituteApi
import com.novack.artgalleryv2.core.artwork.data.fake.artworkPage
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals


class NetworkArtworkRepositoryTest {

    @Test
    fun `repository emits mapped artworks`() = runTest {
        val repository = NetworkArtworkRepository(
            api = FakeArtInstituteApi(
                response = artworkPage(
                    currentPage = 1,
                    totalPages = 1,
                ),
            ),
        )

        val result = repository
            .getArtworks()
            .asSnapshot()

        assertEquals(1, result.size)
        assertEquals("Artwork 1", result.single().title)
    }
}
