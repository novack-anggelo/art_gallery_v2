package com.novack.artgalleryv2.feature.discover.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import com.novack.artgalleryv2.feature.discover.domain.model.ArtworkMetadataField
import com.novack.artgalleryv2.feature.discover.domain.model.ArtworkMetadataVisibility
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceNoChangeReason
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceResult
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferences
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPresentation
import com.novack.artgalleryv2.feature.discover.domain.model.ResizeDirection
import com.novack.artgalleryv2.feature.discover.domain.repository.DiscoverPreferencesStorageResult
import com.novack.artgalleryv2.feature.discover.domain.repository.DiscoverPreferencesStorageStatus
import java.io.File
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
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

        val changed = repository.applyActions(
            listOf(
                DiscoverPreferenceAction.ResizePresentation(ResizeDirection.Smaller),
                DiscoverPreferenceAction.SetMetadataVisibility(
                    ArtworkMetadataField.Date,
                    visible = false,
                ),
            ),
        ).successValue()
        val unchanged = repository.applyActions(
            listOf(DiscoverPreferenceAction.ResizePresentation(ResizeDirection.Smaller)),
        ).successValue()

        val updated = changed.preferences
        assertEquals(original, changed.previousPreferences)
        assertEquals(true, changed.changed)
        assertEquals(DiscoverPresentation.ThumbnailRows, updated.presentation)
        assertEquals(false, updated.metadataVisibility.showArtist)
        assertEquals(false, updated.metadataVisibility.showDate)
        assertEquals(updated, repository.preferences.first())
        assertEquals(
            DiscoverPreferenceNoChangeReason.AlreadyAtSmallest,
            assertIs<DiscoverPreferenceResult.Unchanged>(unchanged.actionResults.single()).reason,
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

    @Test
    fun `read failure emits defaults then recovers`() = runTest {
        val recoveredPreferences = mutablePreferencesOf().apply {
            this[stringPreferencesKey("discover_presentation")] = "thumbnail_rows"
        }
        val repository = DataStoreDiscoverPreferencesRepository(
            FailingDataStore(recoveredPreferences),
        )
        val statuses = mutableListOf<DiscoverPreferencesStorageStatus>()

        val values = repository.preferences
            .onEach { statuses += repository.storageStatus.value }
            .take(2)
            .toList()

        assertEquals(DiscoverPreferences(), values.first())
        assertEquals(DiscoverPresentation.ThumbnailRows, values.last().presentation)
        assertEquals(
            listOf(
                DiscoverPreferencesStorageStatus.Unavailable,
                DiscoverPreferencesStorageStatus.Available,
            ),
            statuses,
        )
    }

    @Test
    fun `writes are rejected while storage is unavailable`() = runTest {
        val repository = DataStoreDiscoverPreferencesRepository(
            FailingDataStore(emptyPreferences()),
        )
        repository.preferences.first()

        assertIs<DiscoverPreferencesStorageResult.Unavailable>(
            repository.replace(DiscoverPreferences()),
        )
    }

    @Test
    fun `write IO failure is returned as unavailable`() = runTest {
        val repository = DataStoreDiscoverPreferencesRepository(
            FailingDataStore(emptyPreferences(), failFirstRead = false, failWrites = true),
        )
        repository.preferences.first()

        assertIs<DiscoverPreferencesStorageResult.Unavailable>(
            repository.replace(DiscoverPreferences()),
        )
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

    private fun <T> DiscoverPreferencesStorageResult<T>.successValue(): T =
        assertIs<DiscoverPreferencesStorageResult.Success<T>>(this).value

    private class FailingDataStore(
        private val recoveredPreferences: Preferences,
        private val failFirstRead: Boolean = true,
        private val failWrites: Boolean = false,
    ) : DataStore<Preferences> {
        private var reads = 0
        override val data: Flow<Preferences> = flow {
            if (failFirstRead && reads++ == 0) throw IOException("read failed")
            emit(recoveredPreferences)
        }

        override suspend fun updateData(
            transform: suspend (t: Preferences) -> Preferences,
        ): Preferences {
            if (failWrites) throw IOException("write failed")
            return transform(recoveredPreferences)
        }
    }
}
