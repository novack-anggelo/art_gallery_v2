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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import com.novack.artgalleryv2.core.domain.model.ArtworkSummary
import com.novack.artgalleryv2.ui.feature.discover.components.DiscoverHeader
import com.novack.artgalleryv2.ui.feature.discover.components.ArtworkCard
import com.novack.artgalleryv2.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DiscoverScreen(
    artworks: LazyPagingItems<ArtworkSummary>,
    onArtworkClick: (Int) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState(),
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .imePadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            DiscoverHeader(state = scrollBehavior.state)
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(1), // TODO: change to adaptative when defined
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
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
}

