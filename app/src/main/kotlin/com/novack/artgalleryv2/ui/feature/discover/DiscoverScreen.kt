package com.novack.artgalleryv2.ui.feature.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.novack.artgalleryv2.R
import com.novack.artgalleryv2.core.domain.model.ArtworkSummary
import com.novack.artgalleryv2.ui.common.ErrorScreen
import com.novack.artgalleryv2.ui.feature.discover.components.ArtworkCard
import com.novack.artgalleryv2.ui.feature.discover.components.ArtworkCardSkeleton
import com.novack.artgalleryv2.ui.feature.discover.components.DiscoverHeader
import com.novack.artgalleryv2.ui.theme.Spacing
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun DiscoverRoute(
    onArtworkClick: (Int) -> Unit,
    viewModel: DiscoverViewModel = koinViewModel(),
) {
    val artworks = viewModel.artworks.collectAsLazyPagingItems()

    DiscoverScreen(artworks = artworks, onArtworkClick = onArtworkClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DiscoverScreen(
    artworks: LazyPagingItems<ArtworkSummary>,
    onArtworkClick: (Int) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState(),
    )

    val refreshState = artworks.loadState.refresh

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DiscoverHeader(state = scrollBehavior.state)
        },
    ) { innerPadding ->
        when {
            artworks.itemCount > 0 -> {
                LoadedState(
                    innerPadding = innerPadding,
                    artworks = artworks,
                    onArtworkClick = onArtworkClick,
                )
            }

            refreshState is LoadState.Loading -> {
                InitialLoadingState(
                    innerPadding = innerPadding,
                )
            }

            refreshState is LoadState.Error -> {
                ErrorScreen(
                    title = stringResource(R.string.discover_error_title),
                    subtitle = stringResource(R.string.discover_error_subtitle),
                    onRetry = { artworks.retry() },
                )
            }

            else -> {
                // Estado vacío: lo trabajaremos después.
            }
        }

    }
}

@Composable
private fun InitialLoadingState(
    innerPadding: PaddingValues,
) {
    val loadingDescription = stringResource(R.string.discover_loading)

    LazyVerticalGrid(
        columns = GridCells.Fixed(1), // TODO: change to adaptative when defined
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
            .imePadding()
            .semantics {
                stateDescription = loadingDescription
                liveRegion = LiveRegionMode.Polite
            },
        contentPadding = PaddingValues(Spacing.SizeXS),
        verticalArrangement = Arrangement.spacedBy(Spacing.SizeS),
        horizontalArrangement = Arrangement.spacedBy(Spacing.SizeS),
    ) {
        items(
            count = 3,
            key = { "discover-skeleton-$it" },
            contentType = { "skeleton" },
        ) {
            ArtworkCardSkeleton(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun LoadedState(
    innerPadding: PaddingValues,
    artworks: LazyPagingItems<ArtworkSummary>,
    onArtworkClick: (Int) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(1), // TODO: change to adaptative when defined
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .consumeWindowInsets(innerPadding)
            .imePadding(),
        contentPadding = PaddingValues(Spacing.SizeXS),
        verticalArrangement = Arrangement.spacedBy(Spacing.SizeS),
        horizontalArrangement = Arrangement.spacedBy(Spacing.SizeS),
    ) {
        items(
            count = artworks.itemCount,
            key = artworks.itemKey { it.id }
        ) { index ->
            val artwork = artworks[index]

            artwork?.let {
                ArtworkCard(
                    artworkSummary = it,
                    onClick = { onArtworkClick(it.id) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

