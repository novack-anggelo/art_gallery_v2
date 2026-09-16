package com.novack.artgalleryv2.ui.feature.discover

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPreferences
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPresentation
import com.novack.artgalleryv2.core.artwork.domain.model.ArtworkSummary
import com.novack.artgalleryv2.R
import com.novack.artgalleryv2.ui.feature.discover.components.ArtworkPreviewProvider
import com.novack.artgalleryv2.core.designsystem.theme.Art_gallery_v2Theme
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoilApi::class)
@Preview(name = "Phone", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(name = "Large text", showBackground = true, widthDp = 360, heightDp = 800, fontScale = 1.8f)
@Preview(name = "Landscape", showBackground = true, widthDp = 800, heightDp = 360)
@Preview(name = "Tablet", showBackground = true, widthDp = 800, heightDp = 1280)
@Composable
private fun DiscoverScreenPreview() {
    DiscoverScreenPreviewContent(DiscoverPresentation.LargeGrid)
}

@Preview(name = "Compact", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(
    name = "Compact dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
    name = "Compact large text",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.8f,
)
@Composable
private fun DiscoverScreenCompactPreview() {
    DiscoverScreenPreviewContent(DiscoverPresentation.CompactGrid)
}

@Preview(name = "Thumbnail rows", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(
    name = "Thumbnail rows dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(
    name = "Thumbnail rows large text",
    showBackground = true,
    widthDp = 360,
    heightDp = 800,
    fontScale = 1.8f,
)
@Composable
private fun DiscoverScreenThumbnailRowsPreview() {
    DiscoverScreenPreviewContent(DiscoverPresentation.ThumbnailRows)
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun DiscoverScreenPreviewContent(presentation: DiscoverPresentation) {
    val artworkFlow = remember {
        val artworks = ArtworkPreviewProvider().values
            // Card fixtures share an ID; a grid needs a unique key for every item.
            .mapIndexed { index, artwork -> artwork.copy(id = index + 1) }
            .toList()
        flowOf(
            PagingData.from(
                data = artworks,
                sourceLoadStates = LoadStates(
                    refresh = LoadState.NotLoading(endOfPaginationReached = false),
                    prepend = LoadState.NotLoading(endOfPaginationReached = true),
                    append = LoadState.NotLoading(endOfPaginationReached = true),
                ),
            )
        )
    }
    val artworks = artworkFlow.collectAsLazyPagingItems()
    val previewHandler = remember {
        AsyncImagePreviewHandler { request ->
            val resource = if (request.data == "preview:portrait") {
                R.drawable.artwork_preview_portrait
            } else {
                R.drawable.artwork_preview_landscape
            }
            checkNotNull(ContextCompat.getDrawable(request.context, resource)).asImage()
        }
    }

    Art_gallery_v2Theme {
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            Surface {
                DiscoverScreen(
                    artworks = artworks,
                    onArtworkClick = {},
                    preferences = DiscoverPreferences(presentation = presentation),
                )
            }
        }
    }
}

@Preview(name = "Initial loading", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun DiscoverScreenLoadingPreview() {
    val artworkFlow = remember {
        flowOf(
            PagingData.from<ArtworkSummary>(
                data = emptyList(),
                sourceLoadStates = LoadStates(
                    refresh = LoadState.Loading,
                    prepend = LoadState.NotLoading(endOfPaginationReached = true),
                    append = LoadState.NotLoading(endOfPaginationReached = true),
                ),
            )
        )
    }
    val artworks = artworkFlow.collectAsLazyPagingItems()
    Art_gallery_v2Theme {
        Surface {
            DiscoverScreen(artworks = artworks, onArtworkClick = {})
        }
    }
}

@Preview(name = "Empty collection", showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun DiscoverScreenEmptyPreview() {
    val artworkFlow = remember {
        flowOf(
            PagingData.from<ArtworkSummary>(
                data = emptyList(),
                sourceLoadStates = LoadStates(
                    refresh = LoadState.NotLoading(endOfPaginationReached = false),
                    prepend = LoadState.NotLoading(endOfPaginationReached = true),
                    append = LoadState.NotLoading(endOfPaginationReached = true),
                ),
            )
        )
    }
    val artworks = artworkFlow.collectAsLazyPagingItems()
    Art_gallery_v2Theme {
        DiscoverScreen(artworks = artworks, onArtworkClick = {})
    }
}
