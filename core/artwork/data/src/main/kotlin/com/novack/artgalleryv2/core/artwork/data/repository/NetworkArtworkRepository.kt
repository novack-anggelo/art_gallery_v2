package com.novack.artgalleryv2.core.artwork.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.novack.artgalleryv2.core.artwork.data.paging.ARTWORK_PAGE_SIZE
import com.novack.artgalleryv2.core.artwork.data.paging.ArtworkPagingSource
import com.novack.artgalleryv2.core.artwork.data.remote.api.ArtInstituteApi
import com.novack.artgalleryv2.core.artwork.domain.model.ArtworkSummary
import com.novack.artgalleryv2.core.artwork.domain.repository.ArtworkRepository
import kotlinx.coroutines.flow.Flow

internal class NetworkArtworkRepository(
    private val api: ArtInstituteApi,
) : ArtworkRepository {
    override fun getArtworks(): Flow<PagingData<ArtworkSummary>> =
        Pager(
            config = PagingConfig(
                pageSize = ARTWORK_PAGE_SIZE,
                initialLoadSize = ARTWORK_PAGE_SIZE,
                prefetchDistance = 5,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                ArtworkPagingSource(api = api)
            },
        ).flow

}
