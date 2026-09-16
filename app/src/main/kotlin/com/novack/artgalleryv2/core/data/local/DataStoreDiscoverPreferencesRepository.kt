package com.novack.artgalleryv2.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.novack.artgalleryv2.feature.discover.domain.model.ArtworkMetadataVisibility
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceTransactionResult
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferences
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPresentation
import com.novack.artgalleryv2.feature.discover.domain.model.apply
import com.novack.artgalleryv2.feature.discover.domain.repository.DiscoverPreferencesRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class DataStoreDiscoverPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : DiscoverPreferencesRepository {
    override val preferences: Flow<DiscoverPreferences> = dataStore.data
        .catch { error ->
            if (error is IOException) emit(emptyPreferences()) else throw error
        }
        .map(Preferences::toDomain)

    override suspend fun applyActions(
        actions: List<DiscoverPreferenceAction>,
    ): DiscoverPreferenceTransactionResult {
        lateinit var result: DiscoverPreferenceTransactionResult
        dataStore.edit { storedPreferences ->
            result = storedPreferences.toDomain().apply(actions)
            if (result.changed) {
                storedPreferences.write(result.preferences)
            }
        }
        return result
    }

    override suspend fun replace(preferences: DiscoverPreferences) {
        dataStore.edit { it.write(preferences) }
    }
}

private val presentationKey = stringPreferencesKey("discover_presentation")
private val showArtistKey = booleanPreferencesKey("discover_show_artist")
private val showDateKey = booleanPreferencesKey("discover_show_date")
private val showMediumKey = booleanPreferencesKey("discover_show_medium")

private fun Preferences.toDomain(): DiscoverPreferences {
    val defaults = DiscoverPreferences()
    return DiscoverPreferences(
        presentation = when (this[presentationKey]) {
            "large_grid" -> DiscoverPresentation.LargeGrid
            "compact_grid" -> DiscoverPresentation.CompactGrid
            "thumbnail_rows" -> DiscoverPresentation.ThumbnailRows
            else -> defaults.presentation
        },
        metadataVisibility = ArtworkMetadataVisibility(
            showArtist = this[showArtistKey] ?: defaults.metadataVisibility.showArtist,
            showDate = this[showDateKey] ?: defaults.metadataVisibility.showDate,
            showMedium = this[showMediumKey] ?: defaults.metadataVisibility.showMedium,
        ),
    )
}

private fun MutablePreferences.write(preferences: DiscoverPreferences) {
    this[presentationKey] = when (preferences.presentation) {
        DiscoverPresentation.LargeGrid -> "large_grid"
        DiscoverPresentation.CompactGrid -> "compact_grid"
        DiscoverPresentation.ThumbnailRows -> "thumbnail_rows"
    }
    this[showArtistKey] = preferences.metadataVisibility.showArtist
    this[showDateKey] = preferences.metadataVisibility.showDate
    this[showMediumKey] = preferences.metadataVisibility.showMedium
}
