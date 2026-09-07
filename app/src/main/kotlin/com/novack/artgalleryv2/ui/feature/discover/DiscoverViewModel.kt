package com.novack.artgalleryv2.ui.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import com.novack.artgalleryv2.core.domain.repository.ArtworkRepository

internal class DiscoverViewModel(
    repository: ArtworkRepository,
) : ViewModel() {
    val artworks = repository
        .getArtworks()
        .cachedIn(viewModelScope)
}
