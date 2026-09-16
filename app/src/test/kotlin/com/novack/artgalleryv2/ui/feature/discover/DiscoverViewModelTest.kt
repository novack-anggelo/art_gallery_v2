package com.novack.artgalleryv2.ui.feature.discover

import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.novack.artgalleryv2.core.artwork.domain.model.ArtworkSummary
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferences
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPresentation
import com.novack.artgalleryv2.feature.discover.domain.model.ResizeDirection
import com.novack.artgalleryv2.feature.discover.domain.model.apply
import com.novack.artgalleryv2.core.artwork.domain.repository.ArtworkRepository
import com.novack.artgalleryv2.feature.discover.domain.repository.DiscoverPreferencesRepository
import com.novack.artgalleryv2.feature.discover.domain.usecase.CoordinateDiscoverPreferenceChangesUseCase
import com.novack.artgalleryv2.core.testing.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
        val viewModel = DiscoverViewModel(
            artworkRepository,
            CoordinateDiscoverPreferenceChangesUseCase(preferencesRepository),
        )

        try {
            val result = viewModel.pagedArtworks.asSnapshot()
            assertEquals(artworks, result)
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    @Test
    fun `ui state combines preferences and undo availability`() = runTest {
        val artworkRepository: ArtworkRepository = mockk()
        val preferencesRepository: DiscoverPreferencesRepository = mockk()
        val expected = DiscoverPreferences(presentation = DiscoverPresentation.ThumbnailRows)
        every { artworkRepository.getArtworks() } returns flowOf(PagingData.empty())
        every { preferencesRepository.preferences } returns flowOf(expected)
        val viewModel = DiscoverViewModel(
            artworkRepository,
            CoordinateDiscoverPreferenceChangesUseCase(preferencesRepository),
        )

        try {
            assertEquals(
                DiscoverUiState(preferences = expected),
                viewModel.uiState.first { it.preferences == expected },
            )
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `preference outcome is exposed as a descriptive UI effect`() = runTest {
        val artworkRepository: ArtworkRepository = mockk()
        val preferencesRepository: DiscoverPreferencesRepository = mockk()
        val preferences = MutableStateFlow(DiscoverPreferences())
        val action = DiscoverPreferenceAction.ResizePresentation(ResizeDirection.Smaller)
        val result = preferences.value.apply(listOf(action))
        every { artworkRepository.getArtworks() } returns flowOf(PagingData.empty())
        every { preferencesRepository.preferences } returns preferences
        coEvery { preferencesRepository.applyActions(listOf(action)) } answers {
            preferences.value = result.preferences
            result
        }
        val viewModel = DiscoverViewModel(
            artworkRepository,
            CoordinateDiscoverPreferenceChangesUseCase(preferencesRepository),
        )

        try {
            val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiEffects.first()
            }
            viewModel.applyDiscoverPreferenceActions(listOf(action))

            assertEquals(
                DiscoverUiEffect.PreferenceChangeApplied(
                    DiscoverPreferenceRequestSource.Customization,
                    result.preferences,
                ),
                effect.await(),
            )
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }
}
