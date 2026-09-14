package com.novack.artgalleryv2.ui.feature.discover.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.novack.artgalleryv2.ui.feature.discover.DiscoverPresentation
import com.novack.artgalleryv2.ui.theme.Art_gallery_v2Theme
import com.novack.artgalleryv2.ui.theme.Spacing

@Preview(name = "Loading", showBackground = true, widthDp = 360)
@Preview(name = "Loading dark", showBackground = true, widthDp = 360, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Loading large text", showBackground = true, widthDp = 360, fontScale = 1.8f)
@Composable
private fun ArtworkCardSkeletonPreview() {
    Art_gallery_v2Theme {
        ArtworkCardSkeleton(modifier = Modifier.padding(Spacing.SizeM))
    }
}

@Preview(name = "Loading compact", showBackground = true, widthDp = 180)
@Composable
private fun ArtworkCardSkeletonCompactPreview() {
    Art_gallery_v2Theme {
        ArtworkCardSkeleton(
            modifier = Modifier.padding(Spacing.SizeXS),
            presentation = DiscoverPresentation.CompactGrid,
        )
    }
}

@Preview(name = "Loading thumbnail row", showBackground = true, widthDp = 360)
@Composable
private fun ArtworkCardSkeletonThumbnailRowPreview() {
    Art_gallery_v2Theme {
        ArtworkCardSkeleton(
            modifier = Modifier.padding(Spacing.SizeXS),
            presentation = DiscoverPresentation.ThumbnailRows,
        )
    }
}

@Preview(name = "Loading title only", showBackground = true, widthDp = 360)
@Composable
private fun ArtworkCardSkeletonTitleOnlyPreview() {
    Art_gallery_v2Theme {
        ArtworkCardSkeleton(
            modifier = Modifier.padding(Spacing.SizeM),
            metadataVisibility = ArtworkMetadataVisibility(
                showArtist = false,
                showDate = false,
                showMedium = false,
            ),
        )
    }
}
