package com.novack.artgalleryv2.core.artwork.data.fake

import com.novack.artgalleryv2.core.artwork.data.paging.ARTWORK_PAGE_SIZE
import com.novack.artgalleryv2.core.artwork.data.remote.api.ArtInstituteApi
import com.novack.artgalleryv2.core.artwork.data.remote.model.ApiConfigDto
import com.novack.artgalleryv2.core.artwork.data.remote.model.ArtworkPageDto
import com.novack.artgalleryv2.core.artwork.data.remote.model.ArtworkSummaryDto
import com.novack.artgalleryv2.core.artwork.data.remote.model.PaginatedResponseDto
import com.novack.artgalleryv2.core.artwork.data.remote.model.PaginationDto

internal class FakeArtInstituteApi(
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

internal fun artworkPage(
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
