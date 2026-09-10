package com.novack.artgalleryv2.core.domain.model

internal data class ArtworkSummary(
    val id: Int,
    val title: String,
    val artist: String?,
    val dateDisplay: String?,
    val image: ArtworkImage,
    val isPublicDomain: Boolean,
    val mediumDisplay: String? = null,
)

internal data class ArtworkImage(
    val url: String,
    val altText: String?,
    val aspectRatio: Float?,
)
