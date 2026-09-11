package com.novack.artgalleryv2.ui.feature.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
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
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.paging.LoadState
import com.novack.artgalleryv2.R
import com.novack.artgalleryv2.ui.feature.discover.components.ArtworkCardSkeleton
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.novack.artgalleryv2.core.domain.model.ArtworkSummary
import com.novack.artgalleryv2.ui.feature.discover.components.DiscoverHeader
import com.novack.artgalleryv2.ui.feature.discover.components.ArtworkCard
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

    val isInitialLoading = artworks.loadState.refresh is LoadState.Loading && artworks.itemCount == 0
    val loadingDescription = stringResource(R.string.discover_loading)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DiscoverHeader(state = scrollBehavior.state)
        },
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(1), // TODO: change to adaptative when defined
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .semantics {
                    if (isInitialLoading) {
                        stateDescription = loadingDescription
                        liveRegion = LiveRegionMode.Polite
                    }
                },
            contentPadding = PaddingValues(Spacing.SizeXS),
            verticalArrangement = Arrangement.spacedBy(Spacing.SizeS),
            horizontalArrangement = Arrangement.spacedBy(Spacing.SizeS),
        ) {
            if (isInitialLoading) {
                items(count = 3, key = { "discover-skeleton-$it" }, contentType = { "skeleton" }) {
                    ArtworkCardSkeleton(modifier = Modifier.fillMaxWidth())
                }
            } else {
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
    }
}

