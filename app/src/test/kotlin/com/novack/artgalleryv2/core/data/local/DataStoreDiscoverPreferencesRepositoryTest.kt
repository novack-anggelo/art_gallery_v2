package com.novack.artgalleryv2.core.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.novack.artgalleryv2.core.domain.model.ArtworkMetadataVisibility
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferenceNoChangeReason
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferenceResult
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferences
import com.novack.artgalleryv2.core.domain.model.DiscoverPresentation
import com.novack.artgalleryv2.core.domain.model.ResizeDirection
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreDiscoverPreferencesRepositoryTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `missing preferences emit defaults`() = runTest {
        val repository = repository(backgroundScope)

        assertEquals(DiscoverPreferences(), repository.preferences.first())
    }

    @Test
    fun `every supported preference value round trips`() = runTest {
        val repository = repository(backgroundScope)

        DiscoverPresentation.entries.forEach { presentation ->
            listOf(true, false).forEach { showArtist ->
                listOf(true, false).forEach { showDate ->
                    listOf(true, false).forEach { showMedium ->
                        val expected = DiscoverPreferences(
                            presentation = presentation,
                            metadataVisibility = ArtworkMetadataVisibility(
                                showArtist = showArtist,
                                showDate = showDate,
                                showMedium = showMedium,
                            ),
                        )

                        repository.replace(expected)

                        assertEquals(expected, repository.preferences.first())
                    }
                }
            }
        }
    }

    @Test
    fun `action is applied to latest stored preferences`() = runTest {
        val repository = repository(backgroundScope)
        val original = DiscoverPreferences(
            presentation = DiscoverPresentation.CompactGrid,
            metadataVisibility = ArtworkMetadataVisibility(showArtist = false),
        )
        repository.replace(original)

        val changed = repository.applyAction(
            DiscoverPreferenceAction.ResizePresentation(ResizeDirection.Smaller),
        )
        val unchanged = repository.applyAction(
            DiscoverPreferenceAction.ResizePresentation(ResizeDirection.Smaller),
        )

        val updated = assertIs<DiscoverPreferenceResult.Changed>(changed).preferences
        assertEquals(DiscoverPresentation.ThumbnailRows, updated.presentation)
        assertEquals(original.metadataVisibility, updated.metadataVisibility)
        assertEquals(updated, repository.preferences.first())
        assertEquals(
            DiscoverPreferenceNoChangeReason.AlreadyAtSmallest,
            assertIs<DiscoverPreferenceResult.Unchanged>(unchanged).reason,
        )
    }

    @Test
    fun `unknown presentation falls back without discarding valid metadata`() = runTest {
        val dataStore = dataStore(backgroundScope)
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey("discover_presentation")] = "future_layout"
            preferences[booleanPreferencesKey("discover_show_artist")] = false
        }

        val result = DataStoreDiscoverPreferencesRepository(dataStore).preferences.first()

        assertEquals(DiscoverPresentation.LargeGrid, result.presentation)
        assertEquals(false, result.metadataVisibility.showArtist)
        assertEquals(true, result.metadataVisibility.showDate)
        assertEquals(true, result.metadataVisibility.showMedium)
    }

    @Test
    fun `repository recreation observes values from the same store`() = runTest {
        val dataStore = dataStore(backgroundScope)
        val expected = DiscoverPreferences(
            presentation = DiscoverPresentation.ThumbnailRows,
            metadataVisibility = ArtworkMetadataVisibility(showMedium = false),
        )
        DataStoreDiscoverPreferencesRepository(dataStore).replace(expected)

        val recreatedRepository = DataStoreDiscoverPreferencesRepository(dataStore)

        assertEquals(expected, recreatedRepository.preferences.first())
    }

    private fun TestScope.repository(scope: CoroutineScope) =
        DataStoreDiscoverPreferencesRepository(dataStore(scope))

    private fun TestScope.dataStore(scope: CoroutineScope): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = scope,
            produceFile = { preferencesFile() },
        )

    private fun preferencesFile(): File =
        temporaryFolder.newFolder().resolve("discover.preferences_pb")
}
