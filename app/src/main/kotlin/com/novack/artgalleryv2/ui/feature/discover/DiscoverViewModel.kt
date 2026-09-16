package com.novack.artgalleryv2.ui.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.novack.artgalleryv2.core.domain.model.ArtworkMetadataField
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferenceResult
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferenceTransactionResult
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferences
import com.novack.artgalleryv2.core.domain.model.DiscoverPresentation
import com.novack.artgalleryv2.core.domain.repository.ArtworkRepository
import com.novack.artgalleryv2.core.domain.repository.DiscoverPreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal enum class DiscoverPreferenceOperation {
    Apply,
    Undo,
    Reset,
}

internal sealed interface DiscoverPreferenceOperationResult {
    val operation: DiscoverPreferenceOperation

    data class Applied(
        override val operation: DiscoverPreferenceOperation,
        val preferences: DiscoverPreferences,
    ) : DiscoverPreferenceOperationResult

    data class Unchanged(
        override val operation: DiscoverPreferenceOperation,
        val actionResults: List<DiscoverPreferenceResult>,
    ) : DiscoverPreferenceOperationResult

    data class Failed(
        override val operation: DiscoverPreferenceOperation,
        val cause: Throwable,
    ) : DiscoverPreferenceOperationResult
}

internal class DiscoverViewModel(
    artworkRepository: ArtworkRepository,
    private val preferencesRepository: DiscoverPreferencesRepository,
) : ViewModel() {
    private val preferenceMutex = Mutex()
    private var undoPreferences: DiscoverPreferences? = null
    private val _canUndo = MutableStateFlow(false)
    private val _preferenceOperationResults = MutableSharedFlow<DiscoverPreferenceOperationResult>()

    val artworks = artworkRepository
        .getArtworks()
        .cachedIn(viewModelScope)

    val preferences = preferencesRepository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DiscoverPreferences(),
    )
    val canUndo = _canUndo.asStateFlow()
    val preferenceOperationResults = _preferenceOperationResults.asSharedFlow()

    fun applyPreferenceActions(actions: List<DiscoverPreferenceAction>) {
        applyTransaction(DiscoverPreferenceOperation.Apply, actions)
    }

    fun resetPreferences() {
        applyTransaction(DiscoverPreferenceOperation.Reset, resetActions)
    }

    fun undoPreferenceChange() {
        viewModelScope.launch {
            preferenceMutex.withLock {
                val preferencesToRestore = undoPreferences ?: return@withLock
                try {
                    preferencesRepository.replace(preferencesToRestore)
                    undoPreferences = null
                    _canUndo.value = false
                    _preferenceOperationResults.emit(
                        DiscoverPreferenceOperationResult.Applied(
                            DiscoverPreferenceOperation.Undo,
                            preferencesToRestore,
                        ),
                    )
                } catch (error: Exception) {
                    emitFailure(DiscoverPreferenceOperation.Undo, error)
                }
            }
        }
    }

    private fun applyTransaction(
        operation: DiscoverPreferenceOperation,
        actions: List<DiscoverPreferenceAction>,
    ) {
        viewModelScope.launch {
            preferenceMutex.withLock {
                try {
                    handleTransaction(operation, preferencesRepository.applyActions(actions))
                } catch (error: Exception) {
                    emitFailure(operation, error)
                }
            }
        }
    }

    private suspend fun handleTransaction(
        operation: DiscoverPreferenceOperation,
        result: DiscoverPreferenceTransactionResult,
    ) {
        if (result.changed) {
            undoPreferences = result.previousPreferences
            _canUndo.value = true
            _preferenceOperationResults.emit(
                DiscoverPreferenceOperationResult.Applied(operation, result.preferences),
            )
        } else {
            _preferenceOperationResults.emit(
                DiscoverPreferenceOperationResult.Unchanged(operation, result.actionResults),
            )
        }
    }

    private suspend fun emitFailure(
        operation: DiscoverPreferenceOperation,
        error: Exception,
    ) {
        if (error is CancellationException) throw error
        _preferenceOperationResults.emit(
            DiscoverPreferenceOperationResult.Failed(operation, error),
        )
    }
}

private val resetActions = listOf(
    DiscoverPreferenceAction.SetPresentation(DiscoverPresentation.LargeGrid),
    DiscoverPreferenceAction.SetMetadataVisibility(ArtworkMetadataField.Artist, visible = true),
    DiscoverPreferenceAction.SetMetadataVisibility(ArtworkMetadataField.Date, visible = true),
    DiscoverPreferenceAction.SetMetadataVisibility(ArtworkMetadataField.Medium, visible = true),
)
