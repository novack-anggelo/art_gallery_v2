package com.novack.artgalleryv2.core.artwork.domain.repository

import androidx.paging.PagingData
import com.novack.artgalleryv2.core.artwork.domain.model.ArtworkSummary
import kotlinx.coroutines.flow.Flow

interface ArtworkRepository {

    fun getArtworks(): Flow<PagingData<ArtworkSummary>>
}
