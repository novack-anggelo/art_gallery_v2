package com.novack.artgalleryv2.ui.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferences
import com.novack.artgalleryv2.core.domain.repository.ArtworkRepository
import com.novack.artgalleryv2.core.domain.repository.DiscoverPreferencesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

internal class DiscoverViewModel(
    artworkRepository: ArtworkRepository,
    preferencesRepository: DiscoverPreferencesRepository,
) : ViewModel() {
    val artworks = artworkRepository
        .getArtworks()
        .cachedIn(viewModelScope)

    val preferences = preferencesRepository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DiscoverPreferences(),
    )
}
