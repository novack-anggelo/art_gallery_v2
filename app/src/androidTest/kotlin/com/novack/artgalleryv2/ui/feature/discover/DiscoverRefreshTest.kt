package com.novack.artgalleryv2.ui.feature.discover

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.compose.collectAsLazyPagingItems
import com.novack.artgalleryv2.core.domain.model.ArtworkImage
import com.novack.artgalleryv2.core.domain.model.ArtworkSummary
import com.novack.artgalleryv2.ui.theme.Art_gallery_v2Theme
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Rule
import org.junit.Test

class DiscoverRefreshTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun emptyCollectionCanBeRefreshedByPulling() {
        val loads = AtomicInteger()
        showCollection(loads, emptyList())
        compose.onNodeWithText("No artworks available").assertIsDisplayed()
        compose.onNode(hasScrollAction()).performTouchInput { swipeDown() }
        compose.waitUntil(5_000) { loads.get() >= 2 }
        compose.onNodeWithText("No artworks available").assertIsDisplayed()
    }

    @Test
    fun failedRefreshPreservesContentAndShowsSnackbarWithoutRetryAction() {
        val loads = AtomicInteger()
        val artwork = ArtworkSummary(
            id = 1,
            title = "Test artwork",
            artist = "Test artist",
            dateDisplay = "1900",
            image = ArtworkImage("", null, null),
            isPublicDomain = true,
        )
        showCollection(loads, listOf(artwork), failRefresh = true)
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("Test artwork").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNode(hasScrollAction()).performTouchInput { swipeDown() }
        compose.waitUntil(5_000) {
            compose.onAllNodesWithText("Unable to refresh artworks").fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText("Test artwork").assertExists()
        compose.onNodeWithText("Retry").assertDoesNotExist()
    }

    private fun showCollection(
        loads: AtomicInteger,
        items: List<ArtworkSummary>,
        failRefresh: Boolean = false,
    ) {
        val flow = Pager(PagingConfig(pageSize = 20)) {
            object : PagingSource<Int, ArtworkSummary>() {
                override fun getRefreshKey(state: PagingState<Int, ArtworkSummary>): Int? = null
                override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ArtworkSummary> {
                    return if (loads.incrementAndGet() > 1 && failRefresh) {
                        LoadResult.Error(IOException("Simulated refresh failure"))
                    } else {
                        LoadResult.Page(items, prevKey = null, nextKey = null)
                    }
                }
            }
        }.flow
        compose.setContent {
            val artworks = flow.collectAsLazyPagingItems()
            Art_gallery_v2Theme {
                DiscoverScreen(artworks, onArtworkClick = {})
            }
        }
    }
}
