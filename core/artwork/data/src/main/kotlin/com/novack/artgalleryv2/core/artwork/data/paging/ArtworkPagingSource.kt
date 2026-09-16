package com.novack.artgalleryv2.core.artwork.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.novack.artgalleryv2.core.artwork.data.mapper.toDomain
import com.novack.artgalleryv2.core.artwork.data.remote.api.ArtInstituteApi
import com.novack.artgalleryv2.core.artwork.data.remote.model.ArtworkPageDto
import com.novack.artgalleryv2.core.artwork.domain.model.ArtworkSummary
import kotlin.coroutines.cancellation.CancellationException

internal const val ARTWORK_PAGE_SIZE = 20

private const val STARTING_PAGE = 1
private const val REQUIRED_IMAGE_FIELD = "image_id"

private const val ARTWORK_SUMMARY_FIELDS =
    "id,title,artist_title,date_display,medium_display,image_id," +
            "is_public_domain,thumbnail"

internal class ArtworkPagingSource(
    private val api: ArtInstituteApi,
) : PagingSource<Int, ArtworkSummary>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArtworkSummary> {
        val requestedPage = params.key ?: STARTING_PAGE

        return try {
            val response = api.getArtworks(
                page = requestedPage,
                limit = ARTWORK_PAGE_SIZE,
                fields = ARTWORK_SUMMARY_FIELDS,
                requiredField = REQUIRED_IMAGE_FIELD,
            )

            LoadResult.Page(
                data = response.toDomain(),
                prevKey = response.previousPageKey(),
                nextKey = response.nextPageKey(),
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ArtworkSummary>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null

        return anchorPage.prevKey?.plus(1)
            ?: anchorPage.nextKey?.minus(1)
    }

}

private fun ArtworkPageDto.previousPageKey(): Int? =
    pagination.currentPage
        .takeIf { it > STARTING_PAGE }
        ?.minus(1)

private fun ArtworkPageDto.nextPageKey(): Int? =
    pagination.currentPage
        .takeIf { it < pagination.totalPages }
        ?.plus(1)
