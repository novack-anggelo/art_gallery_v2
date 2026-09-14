package com.novack.artgalleryv2.core.domain.model

data class DiscoverPreferences(
    val presentation: DiscoverPresentation = DiscoverPresentation.LargeGrid,
    val metadataVisibility: ArtworkMetadataVisibility = ArtworkMetadataVisibility(),
)

enum class DiscoverPresentation {
    LargeGrid,
    CompactGrid,
    ThumbnailRows,
}

data class ArtworkMetadataVisibility(
    val showArtist: Boolean = true,
    val showDate: Boolean = true,
    val showMedium: Boolean = true,
)
