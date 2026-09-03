package com.novack.artgalleryv2.core.data.remote.api

import com.novack.artgalleryv2.core.data.remote.model.ArtworkPageDto
import retrofit2.http.GET
import retrofit2.http.Query

internal interface ArtInstituteApi {

    @GET("artworks")
    suspend fun getArtworks(
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("fields") fields: String,
    ): ArtworkPageDto
}
