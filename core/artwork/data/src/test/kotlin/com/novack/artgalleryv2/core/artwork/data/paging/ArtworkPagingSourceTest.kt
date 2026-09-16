package com.novack.artgalleryv2.core.artwork.data.paging

import androidx.paging.PagingSource
import com.novack.artgalleryv2.core.artwork.data.fake.FakeArtInstituteApi
import com.novack.artgalleryv2.core.artwork.data.fake.artworkPage
import com.novack.artgalleryv2.core.artwork.domain.model.ArtworkSummary
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertSame

class ArtworkPagingSourceTest {

    @Test
    fun `initial load requests first page and returns next key`() = runTest {
        val api = FakeArtInstituteApi(
            response = artworkPage(
                currentPage = 1,
                totalPages = 3,
            ),
        )
        val pagingSource = ArtworkPagingSource(api)

        val result = pagingSource.load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = 60,
                placeholdersEnabled = false,
            )
        )

        val page = assertIs<
                PagingSource.LoadResult.Page<Int, ArtworkSummary>
                >(result)

        assertEquals(1, api.requestedPage)
        assertEquals(ARTWORK_PAGE_SIZE, api.requestedLimit)
        assertEquals("image_id", api.requestedRequiredField)

        assertNull(page.prevKey)
        assertEquals(2, page.nextKey)
    }

    @Test
    fun `last page has no next key`() = runTest {
        val api = FakeArtInstituteApi(
            response = artworkPage(
                currentPage = 3,
                totalPages = 3,
            ),
        )

        val result = ArtworkPagingSource(api).load(
            PagingSource.LoadParams.Append(
                key = 3,
                loadSize = ARTWORK_PAGE_SIZE,
                placeholdersEnabled = false,
            )
        )

        val page = assertIs<
                PagingSource.LoadResult.Page<Int, ArtworkSummary>
                >(result)

        assertEquals(2, page.prevKey)
        assertNull(page.nextKey)
    }

    @Test
    fun `API failure becomes paging error`() = runTest {
        val expected = IOException("No connection")
        val api = FakeArtInstituteApi(failure = expected)

        val result = ArtworkPagingSource(api).load(
            PagingSource.LoadParams.Refresh(
                key = null,
                loadSize = ARTWORK_PAGE_SIZE,
                placeholdersEnabled = false,
            )
        )

        val error = assertIs<
                PagingSource.LoadResult.Error<Int, ArtworkSummary>
                >(result)

        assertSame(expected, error.throwable)
    }
}
