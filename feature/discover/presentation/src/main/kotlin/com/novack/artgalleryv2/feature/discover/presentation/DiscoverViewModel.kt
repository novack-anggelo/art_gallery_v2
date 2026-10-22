package com.novack.artgalleryv2.feature.discover.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.novack.artgalleryv2.core.artwork.domain.repository.ArtworkRepository
import com.novack.artgalleryv2.feature.discover.domain.interpreter.DiscoverCustomizationInterpretation
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.feature.discover.domain.usecase.CoordinateDiscoverPreferenceChangesUseCase
import com.novack.artgalleryv2.feature.discover.domain.usecase.DiscoverPreferenceChangeOutcome
import com.novack.artgalleryv2.feature.discover.domain.usecase.InterpretDiscoverCustomizationUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

internal class DiscoverViewModel(
    artworkRepository: ArtworkRepository,
    private val coordinatePreferenceChanges: CoordinateDiscoverPreferenceChangesUseCase,
    private val interpretCustomization: InterpretDiscoverCustomizationUseCase,
) : ViewModel() {
    private val _uiEffects = MutableSharedFlow<DiscoverUiEffect>()
    private val isCustomizationInProgress = MutableStateFlow(false)

    val pagedArtworks = artworkRepository
        .getArtworks()
        .cachedIn(viewModelScope)
    val uiState = combine(
        coordinatePreferenceChanges.preferences,
        coordinatePreferenceChanges.isUndoPreferenceChangeAvailable,
        coordinatePreferenceChanges.storageStatus,
        isCustomizationInProgress,
        ::DiscoverUiState,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DiscoverUiState(),
    )
    val uiEffects = _uiEffects.asSharedFlow()

    fun submitDiscoverCustomization(prompt: String) {
        if (!isCustomizationInProgress.compareAndSet(expect = false, update = true)) return

        viewModelScope.launch {
            try {
                val currentPreferences = coordinatePreferenceChanges.preferences.first()
                when (val interpretation = interpretCustomization(prompt, currentPreferences)) {
                    is DiscoverCustomizationInterpretation.Supported -> emitPreferenceOutcome(
                        source = DiscoverPreferenceRequestSource.Customization,
                        outcome = coordinatePreferenceChanges.apply(interpretation.actions),
                    )
                    is DiscoverCustomizationInterpretation.Rejected -> _uiEffects.emit(
                        DiscoverUiEffect.CustomizationRejected(interpretation.reason),
                    )
                }
            } finally {
                isCustomizationInProgress.value = false
            }
        }
    }

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
            emitPreferenceOutcome(source, request())
        }
    }

    private suspend fun emitPreferenceOutcome(
        source: DiscoverPreferenceRequestSource,
        outcome: DiscoverPreferenceChangeOutcome,
    ) {
        val effect = when (outcome) {
            is DiscoverPreferenceChangeOutcome.Applied ->
                DiscoverUiEffect.PreferenceChangeApplied(source, outcome.preferences)
            is DiscoverPreferenceChangeOutcome.Unchanged ->
                DiscoverUiEffect.PreferenceChangeUnchanged(source, outcome.actionResults)
            DiscoverPreferenceChangeOutcome.StorageUnavailable ->
                DiscoverUiEffect.PreferenceStorageUnavailable(source)
        }
        _uiEffects.emit(effect)
    }
}
