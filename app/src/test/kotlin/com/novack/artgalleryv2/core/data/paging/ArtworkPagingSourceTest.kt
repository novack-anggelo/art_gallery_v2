package com.novack.artgalleryv2.core.data.paging

import androidx.paging.PagingSource
import com.novack.artgalleryv2.core.data.remote.api.ArtInstituteApi
import com.novack.artgalleryv2.core.data.remote.model.ApiConfigDto
import com.novack.artgalleryv2.core.data.remote.model.ArtworkPageDto
import com.novack.artgalleryv2.core.data.remote.model.ArtworkSummaryDto
import com.novack.artgalleryv2.core.data.remote.model.PaginatedResponseDto
import com.novack.artgalleryv2.core.data.remote.model.PaginationDto
import com.novack.artgalleryv2.core.domain.model.ArtworkSummary
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

    private fun artworkPage(
        currentPage: Int,
        totalPages: Int,
    ): ArtworkPageDto =
        PaginatedResponseDto(
            pagination = PaginationDto(
                total = totalPages * ARTWORK_PAGE_SIZE,
                limit = ARTWORK_PAGE_SIZE,
                currentPage = currentPage,
                totalPages = totalPages,
            ),
            data = listOf(
                ArtworkSummaryDto(
                    id = currentPage,
                    title = "Artwork $currentPage",
                    imageId = "image-$currentPage",
                    isPublicDomain = true,
                )
            ),
            config = ApiConfigDto(
                iiifUrl = "https://www.artic.edu/iiif/2",
            ),
        )
}

private class FakeArtInstituteApi(
    private val response: ArtworkPageDto? = null,
    private val failure: Exception? = null,
) : ArtInstituteApi {

    var requestedPage: Int? = null
        private set

    var requestedLimit: Int? = null
        private set

    var requestedFields: String? = null
        private set

    var requestedRequiredField: String? = null
        private set

    override suspend fun getArtworks(
        page: Int,
        limit: Int,
        fields: String,
        requiredField: String,
    ): ArtworkPageDto {
        requestedPage = page
        requestedLimit = limit
        requestedFields = fields
        requestedRequiredField = requiredField

        failure?.let { throw it }
        return checkNotNull(response)
    }
}
