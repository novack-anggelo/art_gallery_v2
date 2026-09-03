package com.novack.artgalleryv2.core.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PaginatedResponseDto<T>(
    val pagination: PaginationDto,
    val data: List<T>,
    val config: ApiConfigDto,
)

@Serializable
internal data class PaginationDto(
    val total: Int,
    val limit: Int,
    @SerialName("current_page")
    val currentPage: Int,
    @SerialName("total_pages")
    val totalPages: Int,
)

@Serializable
internal data class ApiConfigDto(
    @SerialName("iiif_url")
    val iiifUrl: String,
)
