package com.novack.artgalleryv2.ui.feature.discover.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
