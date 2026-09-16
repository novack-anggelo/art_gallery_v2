package com.novack.artgalleryv2.ui.feature.discover

import com.novack.artgalleryv2.core.domain.model.DiscoverPreferenceResult
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferences

internal data class DiscoverUiState(
    val preferences: DiscoverPreferences = DiscoverPreferences(),
    val isUndoPreferenceChangeAvailable: Boolean = false,
)

internal enum class DiscoverPreferenceRequestSource {
    Customization,
    UndoLastChange,
    ResetToDefaults,
}

internal sealed interface DiscoverUiEffect {
    val source: DiscoverPreferenceRequestSource

    data class PreferenceChangeApplied(
        override val source: DiscoverPreferenceRequestSource,
        val preferences: DiscoverPreferences,
    ) : DiscoverUiEffect

    data class PreferenceChangeUnchanged(
        override val source: DiscoverPreferenceRequestSource,
        val actionResults: List<DiscoverPreferenceResult>,
    ) : DiscoverUiEffect

    data class PreferenceChangeFailed(
        override val source: DiscoverPreferenceRequestSource,
        val cause: Throwable,
    ) : DiscoverUiEffect
}
