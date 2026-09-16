package com.novack.artgalleryv2.feature.discover.domain.usecase

import com.novack.artgalleryv2.feature.discover.domain.model.ArtworkMetadataField
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceTransactionResult
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferences
import com.novack.artgalleryv2.feature.discover.domain.model.ResizeDirection
import com.novack.artgalleryv2.feature.discover.domain.model.apply
import com.novack.artgalleryv2.feature.discover.domain.repository.DiscoverPreferencesRepository
import java.io.IOException
import kotlin.math.max
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class CoordinateDiscoverPreferenceChangesUseCaseTest {
    @Test
    fun `multiple actions create one undoable change`() = runTest {
        val repository = FakePreferencesRepository()
        val useCase = CoordinateDiscoverPreferenceChangesUseCase(repository)
        assertIs<DiscoverPreferenceChangeOutcome.Applied>(
            useCase.apply(listOf(resizeSmaller, hideDate)),
        )
        assertEquals(true, useCase.isUndoPreferenceChangeAvailable.value)
        assertEquals(false, repository.value.metadataVisibility.showDate)

        assertIs<DiscoverPreferenceChangeOutcome.Applied>(useCase.undoLastChange())
        assertEquals(DiscoverPreferences(), repository.value)
        assertEquals(false, useCase.isUndoPreferenceChangeAvailable.value)
    }

    @Test
    fun `no-op reset preserves undo for the previous reset`() = runTest {
        val original = DiscoverPreferences().apply(resizeSmaller).preferences
        val repository = FakePreferencesRepository(original)
        val useCase = CoordinateDiscoverPreferenceChangesUseCase(repository)
        assertIs<DiscoverPreferenceChangeOutcome.Applied>(useCase.resetToDefaults())
        assertIs<DiscoverPreferenceChangeOutcome.Unchanged>(useCase.resetToDefaults())
        assertEquals(true, useCase.isUndoPreferenceChangeAvailable.value)

        useCase.undoLastChange()
        assertEquals(original, repository.value)
    }

    @Test
    fun `latest successful change replaces the previous undo snapshot`() = runTest {
        val repository = FakePreferencesRepository()
        val useCase = CoordinateDiscoverPreferenceChangesUseCase(repository)
        useCase.apply(listOf(resizeSmaller))
        val afterFirstChange = repository.value
        useCase.apply(listOf(hideDate))
        useCase.undoLastChange()

        assertEquals(afterFirstChange, repository.value)
        assertEquals(false, useCase.isUndoPreferenceChangeAvailable.value)
    }

    @Test
    fun `failed writes preserve preferences and existing undo`() = runTest {
        val repository = FakePreferencesRepository()
        val useCase = CoordinateDiscoverPreferenceChangesUseCase(repository)
        useCase.apply(listOf(resizeSmaller))
        val beforeFailure = repository.value
        repository.failure = IOException("write failed")
        assertIs<DiscoverPreferenceChangeOutcome.Failed>(useCase.apply(listOf(hideDate)))
        assertIs<DiscoverPreferenceChangeOutcome.Failed>(useCase.undoLastChange())
        assertEquals(beforeFailure, repository.value)
        assertEquals(true, useCase.isUndoPreferenceChangeAvailable.value)

        repository.failure = null
        useCase.undoLastChange()
        assertEquals(DiscoverPreferences(), repository.value)
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `preference operations are serialized`() = runTest {
        val repository = FakePreferencesRepository()
        val useCase = CoordinateDiscoverPreferenceChangesUseCase(repository)
        repository.gate = CompletableDeferred()
        val first = async { useCase.apply(listOf(resizeSmaller)) }
        runCurrent()
        val second = async { useCase.apply(listOf(hideDate)) }
        runCurrent()

        assertEquals(1, repository.maximumConcurrentWrites)
        repository.gate?.complete(Unit)
        first.await()
        second.await()
        assertEquals(1, repository.maximumConcurrentWrites)
        assertEquals(false, repository.value.metadataVisibility.showDate)
    }

    private class FakePreferencesRepository(
        initialPreferences: DiscoverPreferences = DiscoverPreferences(),
    ) : DiscoverPreferencesRepository {
        private val storedPreferences = MutableStateFlow(initialPreferences)
        override val preferences: Flow<DiscoverPreferences> = storedPreferences
        var failure: IOException? = null
        var gate: CompletableDeferred<Unit>? = null
        var maximumConcurrentWrites = 0
        private var concurrentWrites = 0
        val value: DiscoverPreferences get() = storedPreferences.value

        override suspend fun applyActions(
            actions: List<DiscoverPreferenceAction>,
        ): DiscoverPreferenceTransactionResult = write {
            storedPreferences.value.apply(actions).also { result ->
                if (result.changed) storedPreferences.value = result.preferences
            }
        }

        override suspend fun replace(preferences: DiscoverPreferences) {
            write { storedPreferences.value = preferences }
        }

        private suspend fun <T> write(block: () -> T): T {
            concurrentWrites++
            maximumConcurrentWrites = max(maximumConcurrentWrites, concurrentWrites)
            try {
                gate?.await()
                failure?.let { throw it }
                return block()
            } finally {
                concurrentWrites--
            }
        }
    }

    private companion object {
        val resizeSmaller = DiscoverPreferenceAction.ResizePresentation(ResizeDirection.Smaller)
        val hideDate = DiscoverPreferenceAction.SetMetadataVisibility(
            ArtworkMetadataField.Date,
            visible = false,
        )
    }
}
