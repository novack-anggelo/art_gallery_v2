package com.novack.artgalleryv2.ui.feature.discover.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import androidx.core.content.ContextCompat
import coil3.annotation.ExperimentalCoilApi
import coil3.asImage
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import com.novack.artgalleryv2.R
import com.novack.artgalleryv2.core.domain.model.ArtworkImage
import com.novack.artgalleryv2.core.domain.model.ArtworkMetadataVisibility
import com.novack.artgalleryv2.core.domain.model.ArtworkSummary
import com.novack.artgalleryv2.core.domain.model.DiscoverPresentation
import com.novack.artgalleryv2.core.designsystem.theme.Art_gallery_v2Theme
import com.novack.artgalleryv2.core.designsystem.theme.Spacing

// Illustrative fixtures; the preview handler uses local drawables, never the network.
private val sampleArtwork = ArtworkSummary(
    id = 1,
    title = "Landscape beside the river",
    artist = "Example artist",
    dateDisplay = "1884–86",
    mediumDisplay = "Oil on canvas",
    image = ArtworkImage(
        url = "preview:landscape",
        altText = "Rolling green hills beneath a blue sky.",
        aspectRatio = 1.6f,
    ),
    isPublicDomain = true,
)

internal class ArtworkPreviewProvider : CollectionPreviewParameterProvider<ArtworkSummary>(
    listOf(
        sampleArtwork,
        sampleArtwork.copy(
            title = "A study of flowers in a tall vase beside the studio window",
            dateDisplay = "c. 1900",
            image = ArtworkImage("preview:portrait", "Stems in a tall vase.", 0.625f),
        ),
        sampleArtwork.copy(artist = null),
        sampleArtwork.copy(dateDisplay = null),
        sampleArtwork.copy(artist = null, dateDisplay = null, mediumDisplay = null),
    )
)

@OptIn(ExperimentalCoilApi::class)
@Preview(name = "Light", showBackground = true, widthDp = 360)
@Preview(name = "Dark", showBackground = true, widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large text", showBackground = true, widthDp = 360, fontScale = 1.8f)
@Composable
private fun ArtworkCardPreview(
    @PreviewParameter(ArtworkPreviewProvider::class) artwork: ArtworkSummary,
) {
    ArtworkCardPreviewContent(artwork = artwork)
}

@Preview(name = "Title only", showBackground = true, widthDp = 360)
@Preview(name = "Title only large text", showBackground = true, widthDp = 360, fontScale = 1.8f)
@Composable
private fun ArtworkCardTitleOnlyPreview() {
    ArtworkCardPreviewContent(
        artwork = sampleArtwork,
        metadataVisibility = ArtworkMetadataVisibility(
            showArtist = false,
            showDate = false,
            showMedium = false,
        ),
    )
}

@Preview(name = "Thumbnail row", showBackground = true, widthDp = 360)
@Composable
private fun ArtworkCardThumbnailRowPreview() {
    ArtworkCardPreviewContent(
        artwork = sampleArtwork,
        presentation = DiscoverPresentation.ThumbnailRows,
    )
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun ArtworkCardPreviewContent(
    artwork: ArtworkSummary,
    metadataVisibility: ArtworkMetadataVisibility = ArtworkMetadataVisibility(),
    presentation: DiscoverPresentation = DiscoverPresentation.LargeGrid,
) {
    val previewHandler = AsyncImagePreviewHandler { request ->
        val resource = if (request.data == "preview:portrait") {
            R.drawable.artwork_preview_portrait
        } else {
            R.drawable.artwork_preview_landscape
        }
        checkNotNull(ContextCompat.getDrawable(request.context, resource)).asImage()
    }
    Art_gallery_v2Theme {
        CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
            ArtworkCard(
                artworkSummary = artwork,
                onClick = {},
                modifier = Modifier.padding(Spacing.SizeM),
                metadataVisibility = metadataVisibility,
                presentation = presentation,
            )
        }
    }
}
