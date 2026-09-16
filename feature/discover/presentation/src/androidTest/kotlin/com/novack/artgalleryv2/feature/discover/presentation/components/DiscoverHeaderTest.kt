package com.novack.artgalleryv2.feature.discover.presentation.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.test.junit4.createComposeRule
import com.novack.artgalleryv2.core.designsystem.theme.Art_gallery_v2Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalMaterial3Api::class)
class DiscoverHeaderTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun compactHeaderStopsConsumingScrollAndCanExpandAgain() {
        lateinit var behavior: TopAppBarScrollBehavior
        compose.setContent {
            behavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
            Art_gallery_v2Theme {
                DiscoverHeader(state = behavior.state)
            }
        }

        compose.runOnIdle {
            val limit = behavior.state.heightOffsetLimit
            assertTrue("Measurement must replace the default scroll limit", limit > -Float.MAX_VALUE)
            assertTrue("Expanded header must be taller than compact header", limit < 0f)
            behavior.nestedScrollConnection.onPreScroll(Offset(0f, limit), NestedScrollSource.UserInput)
            assertEquals(limit, behavior.state.heightOffset, 0.01f)
        }
        compose.runOnIdle {
            val consumed = behavior.nestedScrollConnection.onPreScroll(
                Offset(0f, -100f), NestedScrollSource.UserInput,
            )
            assertEquals("The grid must receive scrolling beyond the compact limit", Offset.Zero, consumed)
            val previous = behavior.state.heightOffset
            behavior.nestedScrollConnection.onPreScroll(Offset(0f, 20f), NestedScrollSource.UserInput)
            assertTrue("Upward browsing must expand the header immediately", behavior.state.heightOffset > previous)
        }
    }
}
