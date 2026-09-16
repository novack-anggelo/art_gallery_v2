package com.novack.artgalleryv2.core.artwork.domain.model

data class ArtworkSummary(
    val id: Int,
    val title: String,
    val artist: String?,
    val dateDisplay: String?,
    val image: ArtworkImage,
    val isPublicDomain: Boolean,
    val mediumDisplay: String? = null,
)

data class ArtworkImage(
    val url: String,
    val altText: String?,
    val aspectRatio: Float?,
)
