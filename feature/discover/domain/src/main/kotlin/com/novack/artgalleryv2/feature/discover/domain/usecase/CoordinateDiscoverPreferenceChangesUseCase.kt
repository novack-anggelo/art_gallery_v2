package com.novack.artgalleryv2.feature.discover.domain.usecase

import com.novack.artgalleryv2.feature.discover.domain.model.ArtworkMetadataField
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceResult
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferences
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPresentation
import com.novack.artgalleryv2.feature.discover.domain.repository.DiscoverPreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

sealed interface DiscoverPreferenceChangeOutcome {
    data class Applied(val preferences: DiscoverPreferences) : DiscoverPreferenceChangeOutcome
    data class Unchanged(
        val actionResults: List<DiscoverPreferenceResult>,
    ) : DiscoverPreferenceChangeOutcome
    data class Failed(val cause: Throwable) : DiscoverPreferenceChangeOutcome
}

class CoordinateDiscoverPreferenceChangesUseCase(
    private val repository: DiscoverPreferencesRepository,
) {
    private val mutex = Mutex()
    private var undoPreferences: DiscoverPreferences? = null
    private val _isUndoPreferenceChangeAvailable = MutableStateFlow(false)

    val preferences = repository.preferences
    val isUndoPreferenceChangeAvailable = _isUndoPreferenceChangeAvailable.asStateFlow()

    suspend fun apply(
        actions: List<DiscoverPreferenceAction>,
    ): DiscoverPreferenceChangeOutcome = mutex.withLock {
        applyActions(actions)
    }

    suspend fun resetToDefaults(): DiscoverPreferenceChangeOutcome = mutex.withLock {
        applyActions(resetActions)
    }

    suspend fun undoLastChange(): DiscoverPreferenceChangeOutcome = mutex.withLock {
        val preferencesToRestore = undoPreferences
            ?: return@withLock DiscoverPreferenceChangeOutcome.Unchanged(emptyList())
        try {
            repository.replace(preferencesToRestore)
            undoPreferences = null
            _isUndoPreferenceChangeAvailable.value = false
            DiscoverPreferenceChangeOutcome.Applied(preferencesToRestore)
        } catch (error: Exception) {
            error.asFailure()
        }
    }

    private suspend fun applyActions(
        actions: List<DiscoverPreferenceAction>,
    ): DiscoverPreferenceChangeOutcome = try {
        val result = repository.applyActions(actions)
        if (result.changed) {
            undoPreferences = result.previousPreferences
            _isUndoPreferenceChangeAvailable.value = true
            DiscoverPreferenceChangeOutcome.Applied(result.preferences)
        } else {
            DiscoverPreferenceChangeOutcome.Unchanged(result.actionResults)
        }
    } catch (error: Exception) {
        error.asFailure()
    }
}

private fun Exception.asFailure(): DiscoverPreferenceChangeOutcome.Failed {
    if (this is CancellationException) throw this
    return DiscoverPreferenceChangeOutcome.Failed(this)
}

private val resetActions = listOf(
    DiscoverPreferenceAction.SetPresentation(DiscoverPresentation.LargeGrid),
    DiscoverPreferenceAction.SetMetadataVisibility(ArtworkMetadataField.Artist, visible = true),
    DiscoverPreferenceAction.SetMetadataVisibility(ArtworkMetadataField.Date, visible = true),
    DiscoverPreferenceAction.SetMetadataVisibility(ArtworkMetadataField.Medium, visible = true),
)
