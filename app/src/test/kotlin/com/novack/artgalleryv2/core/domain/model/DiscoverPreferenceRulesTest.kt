package com.novack.artgalleryv2.core.domain.model

import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.junit.Test

class DiscoverPreferenceRulesTest {
    @Test
    fun `defaults use the large grid and show all metadata`() {
        assertEquals(
            DiscoverPreferences(
                presentation = DiscoverPresentation.LargeGrid,
                metadataVisibility = ArtworkMetadataVisibility(
                    showArtist = true,
                    showDate = true,
                    showMedium = true,
                ),
            ),
            DiscoverPreferences(),
        )
    }

    @Test
    fun `smaller steps through every presentation`() {
        val compact = DiscoverPreferences().apply(resize(ResizeDirection.Smaller)).changed()
        val thumbnail = compact.apply(resize(ResizeDirection.Smaller)).changed()

        assertEquals(DiscoverPresentation.CompactGrid, compact.presentation)
        assertEquals(DiscoverPresentation.ThumbnailRows, thumbnail.presentation)
    }

    @Test
    fun `larger steps through every presentation`() {
        val thumbnail = DiscoverPreferences(presentation = DiscoverPresentation.ThumbnailRows)
        val compact = thumbnail.apply(resize(ResizeDirection.Larger)).changed()
        val large = compact.apply(resize(ResizeDirection.Larger)).changed()

        assertEquals(DiscoverPresentation.CompactGrid, compact.presentation)
        assertEquals(DiscoverPresentation.LargeGrid, large.presentation)
    }

    @Test
    fun `resize at either boundary leaves preferences unchanged`() {
        assertUnchanged(
            preferences = DiscoverPreferences(presentation = DiscoverPresentation.ThumbnailRows),
            action = resize(ResizeDirection.Smaller),
            reason = DiscoverPreferenceNoChangeReason.AlreadyAtSmallest,
        )
        assertUnchanged(
            preferences = DiscoverPreferences(),
            action = resize(ResizeDirection.Larger),
            reason = DiscoverPreferenceNoChangeReason.AlreadyAtLargest,
        )
    }

    @Test
    fun `presentation can be selected directly`() {
        val result = DiscoverPreferences().apply(
            DiscoverPreferenceAction.SetPresentation(DiscoverPresentation.ThumbnailRows),
        ).changed()

        assertEquals(DiscoverPresentation.ThumbnailRows, result.presentation)
    }

    @Test
    fun `selecting the current presentation leaves preferences unchanged`() {
        val preferences = DiscoverPreferences()

        assertUnchanged(
            preferences = preferences,
            action = DiscoverPreferenceAction.SetPresentation(DiscoverPresentation.LargeGrid),
            reason = DiscoverPreferenceNoChangeReason.AlreadyApplied,
        )
    }

    @Test
    fun `each metadata field can be changed independently`() {
        ArtworkMetadataField.entries.forEach { field ->
            val hidden = DiscoverPreferences().apply(
                DiscoverPreferenceAction.SetMetadataVisibility(field, visible = false),
            ).changed()
            val shownAgain = hidden.apply(
                DiscoverPreferenceAction.SetMetadataVisibility(field, visible = true),
            ).changed()

            assertEquals(field != ArtworkMetadataField.Artist, hidden.metadataVisibility.showArtist)
            assertEquals(field != ArtworkMetadataField.Date, hidden.metadataVisibility.showDate)
            assertEquals(field != ArtworkMetadataField.Medium, hidden.metadataVisibility.showMedium)
            assertEquals(DiscoverPreferences(), shownAgain)
        }
    }

    @Test
    fun `setting metadata to its current value leaves preferences unchanged`() {
        val preferences = DiscoverPreferences()

        assertUnchanged(
            preferences = preferences,
            action = DiscoverPreferenceAction.SetMetadataVisibility(
                ArtworkMetadataField.Artist,
                visible = true,
            ),
            reason = DiscoverPreferenceNoChangeReason.AlreadyApplied,
        )
    }

    private fun resize(direction: ResizeDirection) =
        DiscoverPreferenceAction.ResizePresentation(direction)

    private fun DiscoverPreferenceResult.changed(): DiscoverPreferences =
        assertIs<DiscoverPreferenceResult.Changed>(this).preferences

    private fun assertUnchanged(
        preferences: DiscoverPreferences,
        action: DiscoverPreferenceAction,
        reason: DiscoverPreferenceNoChangeReason,
    ) {
        val result = assertIs<DiscoverPreferenceResult.Unchanged>(preferences.apply(action))
        assertEquals(preferences, result.preferences)
        assertEquals(reason, result.reason)
    }
}
