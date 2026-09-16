package com.novack.artgalleryv2.ui.feature.discover

import androidx.paging.PagingData
import com.novack.artgalleryv2.core.domain.model.ArtworkMetadataField
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferenceTransactionResult
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferences
import com.novack.artgalleryv2.core.domain.model.ResizeDirection
import com.novack.artgalleryv2.core.domain.model.apply
import com.novack.artgalleryv2.core.domain.repository.ArtworkRepository
import com.novack.artgalleryv2.core.domain.repository.DiscoverPreferencesRepository
import com.novack.artgalleryv2.test.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlin.math.max
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DiscoverPreferenceCoordinatorTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `multiple actions create one undoable change`() = runTest {
        val repository = FakePreferencesRepository()
        val viewModel = viewModel(repository)
        val applied = resultFrom(viewModel) {
            viewModel.applyPreferenceActions(listOf(resizeSmaller, hideDate))
        }

        assertIs<DiscoverPreferenceOperationResult.Applied>(applied)
        assertEquals(true, viewModel.canUndo.value)
        assertEquals(false, repository.value.metadataVisibility.showDate)

        val undone = resultFrom(viewModel, viewModel::undoPreferenceChange)

        assertEquals(DiscoverPreferenceOperation.Undo, undone.operation)
        assertEquals(DiscoverPreferences(), repository.value)
        assertEquals(false, viewModel.canUndo.value)
    }

    @Test
    fun `no-op reset preserves undo for the previous reset`() = runTest {
        val original = DiscoverPreferences().apply(resizeSmaller).preferences
        val repository = FakePreferencesRepository(original)
        val viewModel = viewModel(repository)

        assertIs<DiscoverPreferenceOperationResult.Applied>(
            resultFrom(viewModel, viewModel::resetPreferences),
        )
        assertIs<DiscoverPreferenceOperationResult.Unchanged>(
            resultFrom(viewModel, viewModel::resetPreferences),
        )
        assertEquals(true, viewModel.canUndo.value)

        resultFrom(viewModel, viewModel::undoPreferenceChange)

        assertEquals(original, repository.value)
    }

    @Test
    fun `latest successful change replaces the previous undo snapshot`() = runTest {
        val repository = FakePreferencesRepository()
        val viewModel = viewModel(repository)
        resultFrom(viewModel) { viewModel.applyPreferenceActions(listOf(resizeSmaller)) }
        val afterFirstChange = repository.value
        resultFrom(viewModel) { viewModel.applyPreferenceActions(listOf(hideDate)) }

        resultFrom(viewModel, viewModel::undoPreferenceChange)

        assertEquals(afterFirstChange, repository.value)
        assertEquals(false, viewModel.canUndo.value)
    }

    @Test
    fun `failed writes preserve preferences and existing undo`() = runTest {
        val repository = FakePreferencesRepository()
        val viewModel = viewModel(repository)
        resultFrom(viewModel) { viewModel.applyPreferenceActions(listOf(resizeSmaller)) }
        val beforeFailure = repository.value
        repository.failure = IOException("write failed")

        assertIs<DiscoverPreferenceOperationResult.Failed>(
            resultFrom(viewModel) { viewModel.applyPreferenceActions(listOf(hideDate)) },
        )
        assertIs<DiscoverPreferenceOperationResult.Failed>(
            resultFrom(viewModel, viewModel::undoPreferenceChange),
        )
        assertEquals(beforeFailure, repository.value)
        assertEquals(true, viewModel.canUndo.value)

        repository.failure = null
        resultFrom(viewModel, viewModel::undoPreferenceChange)
        assertEquals(DiscoverPreferences(), repository.value)
    }

    @Test
    fun `preference operations are serialized`() = runTest {
        val repository = FakePreferencesRepository()
        val viewModel = viewModel(repository)
        repository.gate = CompletableDeferred()

        viewModel.applyPreferenceActions(listOf(resizeSmaller))
        runCurrent()
        viewModel.applyPreferenceActions(listOf(hideDate))
        runCurrent()

        assertEquals(1, repository.maximumConcurrentWrites)
        repository.gate?.complete(Unit)
        advanceUntilIdle()
        assertEquals(1, repository.maximumConcurrentWrites)
        assertEquals(false, repository.value.metadataVisibility.showDate)
    }

    private fun viewModel(preferencesRepository: DiscoverPreferencesRepository): DiscoverViewModel {
        val artworkRepository = mockk<ArtworkRepository>()
        every { artworkRepository.getArtworks() } returns flowOf(PagingData.empty())
        return DiscoverViewModel(artworkRepository, preferencesRepository)
    }

    private suspend fun TestScope.resultFrom(
        viewModel: DiscoverViewModel,
        operation: () -> Unit,
    ): DiscoverPreferenceOperationResult {
        val result = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.preferenceOperationResults.first()
        }
        operation()
        advanceUntilIdle()
        return result.await()
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
