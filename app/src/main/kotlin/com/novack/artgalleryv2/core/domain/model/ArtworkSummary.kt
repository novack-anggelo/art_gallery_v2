package com.novack.artgalleryv2.core.domain.model

internal data class ArtworkSummary(
    val id: Int,
    val title: String,
    val artist: String?,
    val date: String?,
    val image: ArtworkImage,
    val isPublicDomain: Boolean,
)

internal data class ArtworkImage(
    val url: String,
    val altText: String?,
    val aspectRatio: Float?,
)
