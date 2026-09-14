package com.novack.artgalleryv2.ui.feature.discover

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.novack.artgalleryv2.core.domain.model.ArtworkImage
import com.novack.artgalleryv2.core.domain.model.ArtworkSummary
import com.novack.artgalleryv2.ui.theme.Art_gallery_v2Theme
import kotlin.math.abs
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DiscoverPresentationTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun defaultPresentationUsesLargeGridWithOneColumnAt360Dp() {
        showGrid()

        val first = compose.onNodeWithText("Artwork 0").getUnclippedBoundsInRoot()
        val second = compose.onNodeWithText("Artwork 1").getUnclippedBoundsInRoot()

        assertTrue(abs((first.left - second.left).value) < 1f)
        assertTrue(second.top > first.bottom)
    }

    @Test
    fun compactGridUsesTwoColumnsAt360Dp() {
        showGrid { DiscoverPresentation.CompactGrid }

        val first = compose.onNodeWithText("Artwork 0").getUnclippedBoundsInRoot()
        val second = compose.onNodeWithText("Artwork 1").getUnclippedBoundsInRoot()

        assertTrue(abs((first.top - second.top).value) < 1f)
        assertTrue(second.left > first.right)
    }

    @Test
    fun thumbnailRowsUseOneFullWidthItemPerRow() {
        showGrid { DiscoverPresentation.ThumbnailRows }

        val first = compose.onNodeWithText("Artwork 0").getUnclippedBoundsInRoot()
        val second = compose.onNodeWithText("Artwork 1").getUnclippedBoundsInRoot()

        assertTrue(abs((first.left - second.left).value) < 1f)
        assertTrue(second.top > first.bottom)
    }

    @Test
    fun gridToRowChangeKeepsVisibleArtworkAnchored() {
        val presentation = mutableStateOf(DiscoverPresentation.CompactGrid)
        showGrid { presentation.value }
        compose.onNode(hasScrollAction()).performScrollToIndex(10)
        val anchoredArtwork = compose.onNodeWithText("Artwork 10").assertIsDisplayed()
        val topBeforeChange = anchoredArtwork.getUnclippedBoundsInRoot().top

        compose.runOnIdle { presentation.value = DiscoverPresentation.ThumbnailRows }

        val topAfterChange = anchoredArtwork.assertIsDisplayed().getUnclippedBoundsInRoot().top
        assertTrue(abs((topBeforeChange - topAfterChange).value) < 1f)
    }

    private fun showGrid(
        presentation: (() -> DiscoverPresentation)? = null,
    ) {
        val artworkFlow = flowOf(PagingData.from(artworks))
        compose.setContent {
            val pagingItems = remember { artworkFlow }.collectAsLazyPagingItems()
            Art_gallery_v2Theme {
                Box(Modifier.width(360.dp).height(700.dp)) {
                    if (presentation == null) {
                        DiscoverScreen(pagingItems, onArtworkClick = {})
                    } else {
                        DiscoverScreen(
                            artworks = pagingItems,
                            onArtworkClick = {},
                            presentation = presentation(),
                        )
                    }
                }
            }
        }
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("Artwork 0").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private val artworks = List(30) { index ->
        ArtworkSummary(
            id = index,
            title = "Artwork $index",
            artist = "Artist $index",
            dateDisplay = "1900",
            mediumDisplay = "Oil on canvas",
            image = ArtworkImage(url = "", altText = null, aspectRatio = null),
            isPublicDomain = true,
        )
    }
}
