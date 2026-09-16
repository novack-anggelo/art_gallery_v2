package com.novack.artgalleryv2.ui.feature.discover.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.novack.artgalleryv2.core.domain.model.ArtworkImage
import com.novack.artgalleryv2.core.domain.model.ArtworkMetadataVisibility
import com.novack.artgalleryv2.core.domain.model.ArtworkSummary
import com.novack.artgalleryv2.core.domain.model.DiscoverPresentation
import com.novack.artgalleryv2.core.designsystem.theme.Art_gallery_v2Theme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ArtworkCardTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun defaultVisibilityShowsAvailableMetadataAndTitle() {
        setCard()

        compose.onNodeWithText("The artwork").assertIsDisplayed()
        compose.onNodeWithText("The artist · 1900").assertIsDisplayed()
        compose.onNodeWithText("Oil on canvas").assertIsDisplayed()
    }

    @Test
    fun allVisibilityCombinationsRenderOnlySelectedMetadata() {
        var visibility by mutableStateOf(ArtworkMetadataVisibility())
        var presentation by mutableStateOf(DiscoverPresentation.LargeGrid)
        compose.setContent {
            Art_gallery_v2Theme {
                ArtworkCard(
                    artwork,
                    onClick = {},
                    metadataVisibility = visibility,
                    presentation = presentation,
                )
            }
        }

        DiscoverPresentation.entries.forEach { selectedPresentation ->
            compose.runOnIdle { presentation = selectedPresentation }
            listOf(true, false).forEach { showArtist ->
                listOf(true, false).forEach { showDate ->
                    listOf(true, false).forEach { showMedium ->
                        compose.runOnIdle {
                            visibility = ArtworkMetadataVisibility(showArtist, showDate, showMedium)
                        }
                        compose.onNodeWithText("The artwork").assertIsDisplayed()
                        assertTextExists("The artist · 1900", showArtist && showDate)
                        assertTextExists("The artist", showArtist && !showDate)
                        assertTextExists("1900", !showArtist && showDate)
                        assertTextExists("Oil on canvas", showMedium)
                    }
                }
            }
        }
    }

    @Test
    fun missingMetadataUsesUnknownArtistWithoutEmptyRows() {
        setCard(artwork = artwork.copy(artist = null, dateDisplay = null, mediumDisplay = null))

        compose.onNodeWithText("Unknown artist").assertIsDisplayed()
        compose.onNodeWithText("Oil on canvas").assertDoesNotExist()
    }

    @Test
    fun hiddenMissingArtistDoesNotShowUnknownArtist() {
        setCard(
            metadataVisibility = ArtworkMetadataVisibility(showArtist = false),
            artwork = artwork.copy(artist = null),
        )

        compose.onNodeWithText("Unknown artist").assertDoesNotExist()
        compose.onNodeWithText("1900").assertIsDisplayed()
    }

    @Test
    fun skeletonRemovesMetadataRowsWhenAllMetadataIsHidden() {
        var visibility by mutableStateOf(ArtworkMetadataVisibility())
        compose.setContent {
            Art_gallery_v2Theme {
                ArtworkCardSkeleton(
                    modifier = Modifier.testTag("skeleton"),
                    metadataVisibility = visibility,
                )
            }
        }

        val skeleton = compose.onNodeWithTag("skeleton")
        val defaultHeight = skeleton.getUnclippedBoundsInRoot().run { bottom - top }
        compose.runOnIdle { visibility = ArtworkMetadataVisibility(false, false, false) }
        val titleOnlyHeight = skeleton.getUnclippedBoundsInRoot().run { bottom - top }
        assertTrue(defaultHeight > titleOnlyHeight)

        compose.runOnIdle { visibility = ArtworkMetadataVisibility(true, false, false) }
        val artistOnlyHeight = skeleton.getUnclippedBoundsInRoot().run { bottom - top }
        compose.runOnIdle { visibility = ArtworkMetadataVisibility(false, true, false) }
        val dateOnlyHeight = skeleton.getUnclippedBoundsInRoot().run { bottom - top }
        assertEquals(artistOnlyHeight, dateOnlyHeight)
    }

    @Test
    fun skeletonReflectsSelectedGridPresentation() {
        var presentation by mutableStateOf(DiscoverPresentation.LargeGrid)
        compose.setContent {
            Art_gallery_v2Theme {
                ArtworkCardSkeleton(
                    modifier = Modifier.width(180.dp).testTag("skeleton"),
                    presentation = presentation,
                )
            }
        }

        val skeleton = compose.onNodeWithTag("skeleton")
        val comfortableHeight = skeleton.getUnclippedBoundsInRoot().run { bottom - top }
        compose.runOnIdle { presentation = DiscoverPresentation.CompactGrid }
        val compactHeight = skeleton.getUnclippedBoundsInRoot().run { bottom - top }

        assertTrue(comfortableHeight > compactHeight)
    }

    @Test
    fun thumbnailRowUsesSquareImageBesideText() {
        compose.setContent {
            Art_gallery_v2Theme {
                ArtworkCard(
                    artworkSummary = artwork,
                    onClick = {},
                    modifier = Modifier.width(360.dp),
                    presentation = DiscoverPresentation.ThumbnailRows,
                )
            }
        }

        val image = compose.onNodeWithTag("artwork-image", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        val title = compose.onNodeWithText("The artwork", useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        assertEquals(96f, (image.right - image.left).value, 1f)
        assertEquals(96f, (image.bottom - image.top).value, 1f)
        assertTrue(title.left > image.right)
    }

    @Test
    fun thumbnailRowSkeletonUsesImageSizedHeight() {
        compose.setContent {
            Art_gallery_v2Theme {
                ArtworkCardSkeleton(
                    modifier = Modifier.width(360.dp).testTag("skeleton-row"),
                    metadataVisibility = ArtworkMetadataVisibility(false, false, false),
                    presentation = DiscoverPresentation.ThumbnailRows,
                )
            }
        }

        val skeleton = compose.onNodeWithTag("skeleton-row").getUnclippedBoundsInRoot()
        assertEquals(120f, (skeleton.bottom - skeleton.top).value, 1f)
    }

    @Test
    fun thumbnailRowAndSkeletonExpandForLargeText() {
        compose.setContent {
            CompositionLocalProvider(
                LocalDensity provides Density(LocalDensity.current.density, fontScale = 2f),
            ) {
                Art_gallery_v2Theme {
                    Column {
                        ArtworkCard(
                            artworkSummary = artwork.copy(
                                title = "A very long artwork title that wraps across lines",
                            ),
                            onClick = {},
                            modifier = Modifier.width(360.dp).testTag("card"),
                            presentation = DiscoverPresentation.ThumbnailRows,
                        )
                        ArtworkCardSkeleton(
                            modifier = Modifier.width(360.dp).testTag("skeleton-row"),
                            presentation = DiscoverPresentation.ThumbnailRows,
                        )
                    }
                }
            }
        }

        val card = compose.onNodeWithTag("card").getUnclippedBoundsInRoot()
        val title = compose.onNodeWithText(
            "A very long artwork title that wraps across lines",
            useUnmergedTree = true,
        )
            .assertIsDisplayed().getUnclippedBoundsInRoot()
        val skeleton = compose.onNodeWithTag("skeleton-row").getUnclippedBoundsInRoot()
        assertTrue(title.bottom <= card.bottom)
        assertTrue((skeleton.bottom - skeleton.top).value > 120f)
    }

    private fun setCard(
        metadataVisibility: ArtworkMetadataVisibility = ArtworkMetadataVisibility(),
        artwork: ArtworkSummary = this.artwork,
    ) {
        compose.setContent {
            Art_gallery_v2Theme {
                ArtworkCard(
                    artworkSummary = artwork,
                    onClick = {},
                    metadataVisibility = metadataVisibility,
                )
            }
        }
    }

    private fun assertTextExists(text: String, exists: Boolean) {
        if (exists) {
            compose.onNodeWithText(text).assertIsDisplayed()
        } else {
            compose.onNodeWithText(text).assertDoesNotExist()
        }
    }

    private val artwork = ArtworkSummary(
        id = 1,
        title = "The artwork",
        artist = "The artist",
        dateDisplay = "1900",
        mediumDisplay = "Oil on canvas",
        image = ArtworkImage(url = "", altText = null, aspectRatio = null),
        isPublicDomain = true,
    )
}
