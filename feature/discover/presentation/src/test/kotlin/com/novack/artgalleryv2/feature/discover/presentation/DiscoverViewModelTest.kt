package com.novack.artgalleryv2.feature.discover.presentation

import androidx.lifecycle.viewModelScope
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.testing.asSnapshot
import com.novack.artgalleryv2.core.artwork.domain.model.ArtworkSummary
import com.novack.artgalleryv2.core.artwork.domain.repository.ArtworkRepository
import com.novack.artgalleryv2.core.testing.MainDispatcherRule
import com.novack.artgalleryv2.feature.discover.domain.interpreter.DiscoverCustomizationInterpretation
import com.novack.artgalleryv2.feature.discover.domain.interpreter.DiscoverCustomizationInterpreter
import com.novack.artgalleryv2.feature.discover.domain.interpreter.DiscoverCustomizationRejection
import com.novack.artgalleryv2.feature.discover.domain.interpreter.DiscoverCustomizationRequest
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceAction
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceNoChangeReason
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferenceResult
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferences
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPresentation
import com.novack.artgalleryv2.feature.discover.domain.model.ResizeDirection
import com.novack.artgalleryv2.feature.discover.domain.model.apply
import com.novack.artgalleryv2.feature.discover.domain.repository.DiscoverPreferencesRepository
import com.novack.artgalleryv2.feature.discover.domain.repository.DiscoverPreferencesStorageResult
import com.novack.artgalleryv2.feature.discover.domain.repository.DiscoverPreferencesStorageStatus
import com.novack.artgalleryv2.feature.discover.domain.usecase.CoordinateDiscoverPreferenceChangesUseCase
import com.novack.artgalleryv2.feature.discover.domain.usecase.InterpretDiscoverCustomizationUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DiscoverViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `viewModel expose artworks from repository`() = runTest {
        val artworkRepository: ArtworkRepository = mockk()
        val preferencesRepository: DiscoverPreferencesRepository = mockk()
        val artworks: List<ArtworkSummary> = listOf(mockk())
        every { preferencesRepository.preferences } returns flowOf(DiscoverPreferences())
        every { preferencesRepository.storageStatus } returns
            MutableStateFlow(DiscoverPreferencesStorageStatus.Available)
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
            interpretCustomization(),
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
        every { preferencesRepository.storageStatus } returns
            MutableStateFlow(DiscoverPreferencesStorageStatus.Available)
        val viewModel = DiscoverViewModel(
            artworkRepository,
            CoordinateDiscoverPreferenceChangesUseCase(preferencesRepository),
            interpretCustomization(),
        )

        try {
            assertEquals(
                DiscoverUiState(
                    preferences = expected,
                    preferenceStorageStatus = DiscoverPreferencesStorageStatus.Available,
                ),
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
        every { preferencesRepository.storageStatus } returns
            MutableStateFlow(DiscoverPreferencesStorageStatus.Available)
        coEvery { preferencesRepository.applyActions(listOf(action)) } answers {
            preferences.value = result.preferences
            DiscoverPreferencesStorageResult.Success(result)
        }
        val viewModel = DiscoverViewModel(
            artworkRepository,
            CoordinateDiscoverPreferenceChangesUseCase(preferencesRepository),
            interpretCustomization(),
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

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `storage failure is exposed without a throwable`() = runTest {
        val artworkRepository: ArtworkRepository = mockk()
        val preferencesRepository: DiscoverPreferencesRepository = mockk()
        every { artworkRepository.getArtworks() } returns flowOf(PagingData.empty())
        every { preferencesRepository.preferences } returns flowOf(DiscoverPreferences())
        every { preferencesRepository.storageStatus } returns
            MutableStateFlow(DiscoverPreferencesStorageStatus.Available)
        coEvery { preferencesRepository.applyActions(any()) } returns
            DiscoverPreferencesStorageResult.Unavailable
        val viewModel = DiscoverViewModel(
            artworkRepository,
            CoordinateDiscoverPreferenceChangesUseCase(preferencesRepository),
            interpretCustomization(),
        )

        try {
            val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiEffects.first()
            }
            viewModel.applyDiscoverPreferenceActions(emptyList())

            assertEquals(
                DiscoverUiEffect.PreferenceStorageUnavailable(
                    DiscoverPreferenceRequestSource.Customization,
                ),
                effect.await(),
            )
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `customization uses current preferences and applies interpreted actions`() = runTest {
        val artworkRepository: ArtworkRepository = mockk()
        val preferencesRepository: DiscoverPreferencesRepository = mockk()
        val preferences = MutableStateFlow(
            DiscoverPreferences(presentation = DiscoverPresentation.CompactGrid),
        )
        val action = DiscoverPreferenceAction.SetPresentation(DiscoverPresentation.ThumbnailRows)
        val result = preferences.value.apply(listOf(action))
        var receivedRequest: DiscoverCustomizationRequest? = null
        every { artworkRepository.getArtworks() } returns flowOf(PagingData.empty())
        every { preferencesRepository.preferences } returns preferences
        every { preferencesRepository.storageStatus } returns
            MutableStateFlow(DiscoverPreferencesStorageStatus.Available)
        coEvery { preferencesRepository.applyActions(listOf(action)) } answers {
            preferences.value = result.preferences
            DiscoverPreferencesStorageResult.Success(result)
        }
        val viewModel = DiscoverViewModel(
            artworkRepository,
            CoordinateDiscoverPreferenceChangesUseCase(preferencesRepository),
            interpretCustomization { request ->
                receivedRequest = request
                DiscoverCustomizationInterpretation.Supported(listOf(action))
            },
        )

        try {
            val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiEffects.first()
            }
            viewModel.submitDiscoverCustomization("  Show thumbnail rows  ")

            assertEquals(
                DiscoverUiEffect.PreferenceChangeApplied(
                    DiscoverPreferenceRequestSource.Customization,
                    result.preferences,
                ),
                effect.await(),
            )
            assertEquals(
                DiscoverCustomizationRequest("Show thumbnail rows", result.previousPreferences),
                receivedRequest,
            )
            assertFalse(viewModel.uiState.first().isCustomizationInProgress)
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `customization rejection preserves preferences and exposes its reason`() = runTest {
        val artworkRepository: ArtworkRepository = mockk()
        val preferencesRepository: DiscoverPreferencesRepository = mockk()
        val preferences = MutableStateFlow(DiscoverPreferences())
        every { artworkRepository.getArtworks() } returns flowOf(PagingData.empty())
        every { preferencesRepository.preferences } returns preferences
        every { preferencesRepository.storageStatus } returns
            MutableStateFlow(DiscoverPreferencesStorageStatus.Available)
        val viewModel = DiscoverViewModel(
            artworkRepository,
            CoordinateDiscoverPreferenceChangesUseCase(preferencesRepository),
            interpretCustomization {
                DiscoverCustomizationInterpretation.Rejected(
                    DiscoverCustomizationRejection.QuotaExceeded,
                )
            },
        )

        try {
            val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiEffects.first()
            }
            viewModel.submitDiscoverCustomization("Make images smaller")

            assertEquals(
                DiscoverUiEffect.CustomizationRejected(
                    DiscoverCustomizationRejection.QuotaExceeded,
                ),
                effect.await(),
            )
            assertEquals(DiscoverPreferences(), preferences.value)
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `bounded customization exposes an unchanged preference result`() = runTest {
        val artworkRepository: ArtworkRepository = mockk()
        val preferencesRepository: DiscoverPreferencesRepository = mockk()
        val preferences = MutableStateFlow(
            DiscoverPreferences(presentation = DiscoverPresentation.ThumbnailRows),
        )
        val action = DiscoverPreferenceAction.ResizePresentation(ResizeDirection.Smaller)
        val result = preferences.value.apply(listOf(action))
        every { artworkRepository.getArtworks() } returns flowOf(PagingData.empty())
        every { preferencesRepository.preferences } returns preferences
        every { preferencesRepository.storageStatus } returns
            MutableStateFlow(DiscoverPreferencesStorageStatus.Available)
        coEvery { preferencesRepository.applyActions(listOf(action)) } returns
            DiscoverPreferencesStorageResult.Success(result)
        val viewModel = DiscoverViewModel(
            artworkRepository,
            CoordinateDiscoverPreferenceChangesUseCase(preferencesRepository),
            interpretCustomization {
                DiscoverCustomizationInterpretation.Supported(listOf(action))
            },
        )

        try {
            val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiEffects.first()
            }
            viewModel.submitDiscoverCustomization("Smaller")

            assertEquals(
                DiscoverUiEffect.PreferenceChangeUnchanged(
                    DiscoverPreferenceRequestSource.Customization,
                    result.actionResults,
                ),
                effect.await(),
            )
            assertEquals(
                DiscoverPreferenceNoChangeReason.AlreadyAtSmallest,
                assertIs<DiscoverPreferenceResult.Unchanged>(result.actionResults.single()).reason,
            )
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `only one customization is interpreted at a time`() = runTest {
        val artworkRepository: ArtworkRepository = mockk()
        val preferencesRepository: DiscoverPreferencesRepository = mockk()
        val gate = CompletableDeferred<Unit>()
        var interpretationCount = 0
        every { artworkRepository.getArtworks() } returns flowOf(PagingData.empty())
        every { preferencesRepository.preferences } returns flowOf(DiscoverPreferences())
        every { preferencesRepository.storageStatus } returns
            MutableStateFlow(DiscoverPreferencesStorageStatus.Available)
        val viewModel = DiscoverViewModel(
            artworkRepository,
            CoordinateDiscoverPreferenceChangesUseCase(preferencesRepository),
            interpretCustomization {
                interpretationCount++
                gate.await()
                DiscoverCustomizationInterpretation.Rejected(
                    DiscoverCustomizationRejection.UnsupportedRequest,
                )
            },
        )

        try {
            viewModel.submitDiscoverCustomization("First")
            runCurrent()
            assertTrue(viewModel.uiState.first { it.isCustomizationInProgress }
                .isCustomizationInProgress)

            viewModel.submitDiscoverCustomization("Second")
            runCurrent()
            assertEquals(1, interpretationCount)

            gate.complete(Unit)
            runCurrent()
            assertFalse(viewModel.uiState.first().isCustomizationInProgress)
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    @Test
    fun `customization storage failure is exposed without changing preferences`() = runTest {
        val artworkRepository: ArtworkRepository = mockk()
        val preferencesRepository: DiscoverPreferencesRepository = mockk()
        val preferences = MutableStateFlow(DiscoverPreferences())
        val action = DiscoverPreferenceAction.ResizePresentation(ResizeDirection.Smaller)
        every { artworkRepository.getArtworks() } returns flowOf(PagingData.empty())
        every { preferencesRepository.preferences } returns preferences
        every { preferencesRepository.storageStatus } returns
            MutableStateFlow(DiscoverPreferencesStorageStatus.Available)
        coEvery { preferencesRepository.applyActions(listOf(action)) } returns
            DiscoverPreferencesStorageResult.Unavailable
        val viewModel = DiscoverViewModel(
            artworkRepository,
            CoordinateDiscoverPreferenceChangesUseCase(preferencesRepository),
            interpretCustomization {
                DiscoverCustomizationInterpretation.Supported(listOf(action))
            },
        )

        try {
            val effect = backgroundScope.async(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.uiEffects.first()
            }
            viewModel.submitDiscoverCustomization("Smaller")

            assertEquals(
                DiscoverUiEffect.PreferenceStorageUnavailable(
                    DiscoverPreferenceRequestSource.Customization,
                ),
                effect.await(),
            )
            assertEquals(DiscoverPreferences(), preferences.value)
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    private fun interpretCustomization(
        interpretation: suspend (
            DiscoverCustomizationRequest,
        ) -> DiscoverCustomizationInterpretation = {
            DiscoverCustomizationInterpretation.Rejected(
                DiscoverCustomizationRejection.InvalidRequest,
            )
        },
    ) = InterpretDiscoverCustomizationUseCase(
        DiscoverCustomizationInterpreter(interpretation),
    )
}
