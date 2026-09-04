package com.novack.artgalleryv2.core.domain.repository

import androidx.paging.PagingData
import com.novack.artgalleryv2.core.domain.model.ArtworkSummary
import kotlinx.coroutines.flow.Flow

internal interface ArtworkRepository {

    fun getArtworks(): Flow<PagingData<ArtworkSummary>>
}
