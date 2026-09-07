package com.novack.artgalleryv2.ui.feature.discover

import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.novack.artgalleryv2.core.domain.model.ArtworkSummary
import com.novack.artgalleryv2.core.domain.repository.ArtworkRepository
import com.novack.artgalleryv2.test.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class DiscoverViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `viewModel expose artworks from repository`() = runTest {
        val repository: ArtworkRepository = mockk()
        val artworks: List<ArtworkSummary> = listOf(mockk())
        every { repository.getArtworks() } returns flowOf(PagingData.from(artworks))
        val pagingData = PagingData.from(
            data = artworks,
            sourceLoadStates = LoadStates(
                refresh = LoadState.NotLoading(endOfPaginationReached = false),
                prepend = LoadState.NotLoading(endOfPaginationReached = true),
                append = LoadState.NotLoading(endOfPaginationReached = true),
            ),
        )

        every { repository.getArtworks() } returns flowOf(pagingData)
        val viewModel = DiscoverViewModel(repository)

        try {
            val result = viewModel.artworks.asSnapshot()
            assertEquals(artworks, result)
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }
}