package com.novack.artgalleryv2.ui.feature.discover

import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.novack.artgalleryv2.core.domain.model.ArtworkSummary
import com.novack.artgalleryv2.core.domain.model.DiscoverPreferences
import com.novack.artgalleryv2.core.domain.model.DiscoverPresentation
import com.novack.artgalleryv2.core.domain.repository.ArtworkRepository
import com.novack.artgalleryv2.core.domain.repository.DiscoverPreferencesRepository
import com.novack.artgalleryv2.test.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DiscoverViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `viewModel expose artworks from repository`() = runTest {
        val artworkRepository: ArtworkRepository = mockk()
        val preferencesRepository: DiscoverPreferencesRepository = mockk()
        val artworks: List<ArtworkSummary> = listOf(mockk())
        every { preferencesRepository.preferences } returns flowOf(DiscoverPreferences())
        val pagingData = PagingData.from(
            data = artworks,
            sourceLoadStates = LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
        )

        every { artworkRepository.getArtworks() } returns flowOf(pagingData)
        val viewModel = DiscoverViewModel(artworkRepository, preferencesRepository)

        try {
            val result = viewModel.artworks.asSnapshot()
            assertEquals(artworks, result)
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    @Test
    fun `viewModel exposes preferences from repository`() = runTest {
        val artworkRepository: ArtworkRepository = mockk()
        val preferencesRepository: DiscoverPreferencesRepository = mockk()
        val expected = DiscoverPreferences(presentation = DiscoverPresentation.ThumbnailRows)
        every { artworkRepository.getArtworks() } returns flowOf(PagingData.empty())
        every { preferencesRepository.preferences } returns flowOf(expected)
        val viewModel = DiscoverViewModel(artworkRepository, preferencesRepository)

        try {
            assertEquals(expected, viewModel.preferences.first { it == expected })
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }
}
