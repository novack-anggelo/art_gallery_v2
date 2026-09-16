package com.novack.artgalleryv2.ui.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.core.artwork.domain.repository.ArtworkRepository
import com.novack.artgalleryv2.feature.discover.domain.usecase.CoordinateDiscoverPreferenceChangesUseCase
import com.novack.artgalleryv2.feature.discover.domain.usecase.DiscoverPreferenceChangeOutcome
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class DiscoverViewModel(
    artworkRepository: ArtworkRepository,
    private val coordinatePreferenceChanges: CoordinateDiscoverPreferenceChangesUseCase,
) : ViewModel() {
    private val _uiEffects = MutableSharedFlow<DiscoverUiEffect>()

    val pagedArtworks = artworkRepository
        .getArtworks()
        .cachedIn(viewModelScope)
    val uiState = combine(
        coordinatePreferenceChanges.preferences,
        coordinatePreferenceChanges.isUndoPreferenceChangeAvailable,
        ::DiscoverUiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DiscoverUiState(),
    )
    val uiEffects = _uiEffects.asSharedFlow()

    fun applyDiscoverPreferenceActions(actions: List<DiscoverPreferenceAction>) {
        executePreferenceRequest(DiscoverPreferenceRequestSource.Customization) {
            coordinatePreferenceChanges.apply(actions)
        }
    }

    fun undoLastDiscoverPreferenceChange() {
        executePreferenceRequest(DiscoverPreferenceRequestSource.UndoLastChange) {
            coordinatePreferenceChanges.undoLastChange()
        }
    }

    fun resetDiscoverPreferencesToDefaults() {
        executePreferenceRequest(DiscoverPreferenceRequestSource.ResetToDefaults) {
            coordinatePreferenceChanges.resetToDefaults()
        }
    }

    private fun executePreferenceRequest(
        source: DiscoverPreferenceRequestSource,
        request: suspend () -> DiscoverPreferenceChangeOutcome,
    ) {
        viewModelScope.launch {
            val effect = when (val outcome = request()) {
                is DiscoverPreferenceChangeOutcome.Applied ->
                    DiscoverUiEffect.PreferenceChangeApplied(source, outcome.preferences)
                is DiscoverPreferenceChangeOutcome.Unchanged ->
                    DiscoverUiEffect.PreferenceChangeUnchanged(source, outcome.actionResults)
                is DiscoverPreferenceChangeOutcome.Failed ->
                    DiscoverUiEffect.PreferenceChangeFailed(source, outcome.cause)
            }
            _uiEffects.emit(effect)
        }
    }
}
