package com.novack.artgalleryv2.feature.discover.data.repository

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
import com.novack.artgalleryv2.feature.discover.domain.repository.DiscoverPreferencesStorageResult
import com.novack.artgalleryv2.feature.discover.domain.repository.DiscoverPreferencesStorageStatus
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.min
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retryWhen

class DataStoreDiscoverPreferencesRepository(
    private val dataStore: DataStore<Preferences>,
) : DiscoverPreferencesRepository {
    private val _storageStatus = MutableStateFlow(DiscoverPreferencesStorageStatus.Loading)
    override val storageStatus = _storageStatus.asStateFlow()

    override val preferences: Flow<DiscoverPreferences> = dataStore.data
        .onEach { _storageStatus.value = DiscoverPreferencesStorageStatus.Available }
        .retryWhen { error, attempt ->
            if (error !is IOException) return@retryWhen false
            _storageStatus.value = DiscoverPreferencesStorageStatus.Unavailable
            emit(emptyPreferences())
            delay(min(attempt + 1, MAX_RETRY_SECONDS) * MILLIS_PER_SECOND)
            true
        }
        .map(Preferences::toDomain)

    override suspend fun applyActions(
        actions: List<DiscoverPreferenceAction>,
    ): DiscoverPreferencesStorageResult<DiscoverPreferenceTransactionResult> {
        if (_storageStatus.value == DiscoverPreferencesStorageStatus.Unavailable) {
            return DiscoverPreferencesStorageResult.Unavailable
        }
        lateinit var result: DiscoverPreferenceTransactionResult
        return try {
            dataStore.edit { storedPreferences ->
                result = storedPreferences.toDomain().apply(actions)
                if (result.changed) storedPreferences.write(result.preferences)
            }
            DiscoverPreferencesStorageResult.Success(result)
        } catch (error: CancellationException) {
            throw error
        } catch (_: IOException) {
            DiscoverPreferencesStorageResult.Unavailable
        }
    }

    override suspend fun replace(
        preferences: DiscoverPreferences,
    ): DiscoverPreferencesStorageResult<Unit> {
        if (_storageStatus.value == DiscoverPreferencesStorageStatus.Unavailable) {
            return DiscoverPreferencesStorageResult.Unavailable
        }
        return try {
            dataStore.edit { it.write(preferences) }
            DiscoverPreferencesStorageResult.Success(Unit)
        } catch (error: CancellationException) {
            throw error
        } catch (_: IOException) {
            DiscoverPreferencesStorageResult.Unavailable
        }
    }
}

private const val MILLIS_PER_SECOND = 1_000L
private const val MAX_RETRY_SECONDS = 30L

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
