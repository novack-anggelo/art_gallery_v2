package com.novack.artgalleryv2.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal typealias ArtworkPageDto =
        PaginatedResponseDto<ArtworkSummaryDto>

@Serializable
internal data class ArtworkSummaryDto(
    val id: Int,
    val title: String,
    @SerialName("artist_title")
    val artistTitle: String? = null,
    @SerialName("date_display")
    val dateDisplay: String? = null,
    @SerialName("image_id")
    val imageId: String? = null,
    @SerialName("is_public_domain")
    val isPublicDomain: Boolean? = null,
    val thumbnail: ArtworkThumbnailDto? = null,
)

@Serializable
internal data class ArtworkThumbnailDto(
    val width: Int? = null,
    val height: Int? = null,
    @SerialName("alt_text")
    val altText: String? = null,
)
