package com.novack.artgalleryv2.core.domain.model

sealed interface DiscoverPreferenceAction {
    data class SetPresentation(val presentation: DiscoverPresentation) : DiscoverPreferenceAction

    data class ResizePresentation(val direction: ResizeDirection) : DiscoverPreferenceAction

    data class SetMetadataVisibility(
        val field: ArtworkMetadataField,
        val visible: Boolean,
    ) : DiscoverPreferenceAction
}

enum class ResizeDirection {
    Smaller,
    Larger,
}

enum class ArtworkMetadataField {
    Artist,
    Date,
    Medium,
}

sealed interface DiscoverPreferenceResult {
    val preferences: DiscoverPreferences

    data class Changed(
        override val preferences: DiscoverPreferences,
    ) : DiscoverPreferenceResult

    data class Unchanged(
        override val preferences: DiscoverPreferences,
        val reason: DiscoverPreferenceNoChangeReason,
    ) : DiscoverPreferenceResult
}

enum class DiscoverPreferenceNoChangeReason {
    AlreadyApplied,
    AlreadyAtSmallest,
    AlreadyAtLargest,
}

fun DiscoverPreferences.apply(
    action: DiscoverPreferenceAction,
): DiscoverPreferenceResult = when (action) {
    is DiscoverPreferenceAction.SetPresentation -> updatePresentation(action.presentation)
    is DiscoverPreferenceAction.ResizePresentation -> resizePresentation(action.direction)
    is DiscoverPreferenceAction.SetMetadataVisibility -> updateMetadata(action)
}

private fun DiscoverPreferences.resizePresentation(
    direction: ResizeDirection,
): DiscoverPreferenceResult {
    val nextPresentation = when (direction) {
        ResizeDirection.Smaller -> when (presentation) {
            DiscoverPresentation.LargeGrid -> DiscoverPresentation.CompactGrid
            DiscoverPresentation.CompactGrid -> DiscoverPresentation.ThumbnailRows
            DiscoverPresentation.ThumbnailRows -> null
        }
        ResizeDirection.Larger -> when (presentation) {
            DiscoverPresentation.LargeGrid -> null
            DiscoverPresentation.CompactGrid -> DiscoverPresentation.LargeGrid
            DiscoverPresentation.ThumbnailRows -> DiscoverPresentation.CompactGrid
        }
    }
    return nextPresentation?.let(::updatePresentation) ?: DiscoverPreferenceResult.Unchanged(
        preferences = this,
        reason = when (direction) {
            ResizeDirection.Smaller -> DiscoverPreferenceNoChangeReason.AlreadyAtSmallest
            ResizeDirection.Larger -> DiscoverPreferenceNoChangeReason.AlreadyAtLargest
        },
    )
}

private fun DiscoverPreferences.updatePresentation(
    newPresentation: DiscoverPresentation,
): DiscoverPreferenceResult = if (presentation == newPresentation) {
    DiscoverPreferenceResult.Unchanged(this, DiscoverPreferenceNoChangeReason.AlreadyApplied)
} else {
    DiscoverPreferenceResult.Changed(copy(presentation = newPresentation))
}

private fun DiscoverPreferences.updateMetadata(
    action: DiscoverPreferenceAction.SetMetadataVisibility,
): DiscoverPreferenceResult {
    val currentValue = when (action.field) {
        ArtworkMetadataField.Artist -> metadataVisibility.showArtist
        ArtworkMetadataField.Date -> metadataVisibility.showDate
        ArtworkMetadataField.Medium -> metadataVisibility.showMedium
    }
    if (currentValue == action.visible) {
        return DiscoverPreferenceResult.Unchanged(
            this,
            DiscoverPreferenceNoChangeReason.AlreadyApplied,
        )
    }

    val updatedVisibility = when (action.field) {
        ArtworkMetadataField.Artist -> metadataVisibility.copy(showArtist = action.visible)
        ArtworkMetadataField.Date -> metadataVisibility.copy(showDate = action.visible)
        ArtworkMetadataField.Medium -> metadataVisibility.copy(showMedium = action.visible)
    }
    return DiscoverPreferenceResult.Changed(copy(metadataVisibility = updatedVisibility))
}
