package com.novack.artgalleryv2.feature.discover.presentation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
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
import com.novack.artgalleryv2.feature.discover.presentation.R
import com.novack.artgalleryv2.core.artwork.domain.model.ArtworkSummary
import com.novack.artgalleryv2.feature.discover.domain.model.ArtworkMetadataVisibility
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferences
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPresentation
import com.novack.artgalleryv2.core.designsystem.component.ErrorScreen
import com.novack.artgalleryv2.core.designsystem.theme.Spacing
import com.novack.artgalleryv2.feature.discover.presentation.components.ArtworkCard
import com.novack.artgalleryv2.feature.discover.presentation.components.ArtworkCardSkeleton
import com.novack.artgalleryv2.feature.discover.presentation.components.DiscoverHeader
import org.koin.androidx.compose.koinViewModel

@Composable
fun DiscoverRoute(
    onArtworkClick: (Int) -> Unit,
) {
    DiscoverRoute(
        onArtworkClick = onArtworkClick,
        viewModel = koinViewModel(),
    )
}

@Composable
internal fun DiscoverRoute(
    onArtworkClick: (Int) -> Unit,
    viewModel: DiscoverViewModel,
) {
    val artworks = viewModel.pagedArtworks.collectAsLazyPagingItems()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DiscoverScreen(
        artworks = artworks,
        onArtworkClick = onArtworkClick,
        preferences = uiState.preferences,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DiscoverScreen(
    artworks: LazyPagingItems<ArtworkSummary>,
    onArtworkClick: (Int) -> Unit,
    preferences: DiscoverPreferences = DiscoverPreferences(),
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState(),
    )

    val refreshState = artworks.loadState.refresh
    val snackbarHostState = remember { SnackbarHostState() }
    var pullRequested by remember { mutableStateOf(false) }
    val refreshErrorMessage = stringResource(R.string.discover_refresh_error)

    // A new Loading → Error transition can show another snackbar after the next pull.
    LaunchedEffect(refreshState) {
        if (refreshState !is LoadState.Loading) pullRequested = false
        if (refreshState is LoadState.Error && artworks.itemCount > 0) {
            snackbarHostState.showSnackbar(message = refreshErrorMessage)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        },
        topBar = {
            DiscoverHeader(state = scrollBehavior.state)
        },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = refreshState is LoadState.Loading &&
                (artworks.itemCount > 0 || pullRequested),
            onRefresh = {
                if (refreshState !is LoadState.Loading) {
                    snackbarHostState.currentSnackbarData?.dismiss()
                    pullRequested = true
                    artworks.refresh()
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .imePadding(),
        ) {
            when {
                artworks.itemCount > 0 -> LoadedState(
                    artworks = artworks,
                    onArtworkClick = onArtworkClick,
                    metadataVisibility = preferences.metadataVisibility,
                    presentation = preferences.presentation,
                )
                refreshState is LoadState.Loading && !pullRequested ->
                    InitialLoadingState(
                        preferences.metadataVisibility,
                        preferences.presentation,
                    )
                refreshState is LoadState.Error -> ErrorScreen(
                    title = stringResource(R.string.discover_error_title),
                    subtitle = stringResource(R.string.discover_error_subtitle),
                    onRetry = { artworks.retry() },
                    modifier = Modifier.fillMaxSize().padding(Spacing.SizeM),
                )
                else -> DiscoverEmptyState()
            }
        }
    }
}

@Composable
private fun DiscoverEmptyState() {
    // Even empty content needs a scrollable child to dispatch pull gestures.
    BoxWithConstraints(Modifier.fillMaxSize()) {
        ErrorScreen(
            title = stringResource(R.string.discover_empty_title),
            subtitle = stringResource(R.string.discover_empty_subtitle),
            onRetry = null,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .padding(Spacing.SizeL),
        )
    }
}

@Composable
private fun InitialLoadingState(
    metadataVisibility: ArtworkMetadataVisibility,
    presentation: DiscoverPresentation,
) {
    val loadingDescription = stringResource(R.string.discover_loading)

    LazyVerticalGrid(
        columns = presentation.gridCells(),
        modifier = Modifier
            .fillMaxSize()
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
            ArtworkCardSkeleton(
                modifier = Modifier.fillMaxWidth(),
                metadataVisibility = metadataVisibility,
                presentation = presentation,
            )
        }
    }
}

@Composable
private fun LoadedState(
    artworks: LazyPagingItems<ArtworkSummary>,
    onArtworkClick: (Int) -> Unit,
    metadataVisibility: ArtworkMetadataVisibility,
    presentation: DiscoverPresentation,
) {
    LazyVerticalGrid(
        columns = presentation.gridCells(),
        modifier = Modifier.fillMaxSize(),
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
                    metadataVisibility = metadataVisibility,
                    presentation = presentation,
                )
            }
        }

        when (artworks.loadState.append) {
            is LoadState.Loading -> item(
                key = "discover-append-loading",
                span = { GridItemSpan(maxLineSpan) },
                contentType = "append-loading",
            ) {
                val loadingDescription = stringResource(R.string.discover_loading_more)
                Box(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.SizeL),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.semantics {
                            stateDescription = loadingDescription
                            liveRegion = LiveRegionMode.Polite
                        },
                    )
                }
            }
            is LoadState.Error -> item(
                key = "discover-append-error",
                span = { GridItemSpan(maxLineSpan) },
                contentType = "append-error",
            ) {
                ErrorScreen(
                    title = stringResource(R.string.discover_append_error),
                    onRetry = { artworks.retry() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.SizeL)
                        .semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            is LoadState.NotLoading -> Unit
        }
    }
}

private fun DiscoverPresentation.gridCells(): GridCells = when (this) {
    DiscoverPresentation.ThumbnailRows -> GridCells.Fixed(1)
    else -> GridCells.Adaptive(minSize = checkNotNull(minimumCellWidth))
}
