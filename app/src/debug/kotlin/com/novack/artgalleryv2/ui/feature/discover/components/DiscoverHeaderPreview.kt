package com.novack.artgalleryv2.ui.feature.discover.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.novack.artgalleryv2.core.designsystem.theme.Art_gallery_v2Theme

internal class HeaderCollapsePreviewProvider : CollectionPreviewParameterProvider<Float>(
    listOf(0f, 0.5f, 1f)
)

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Header", widthDp = 360, showBackground = true)
@Preview(name = "Large text", widthDp = 360, fontScale = 1.8f, showBackground = true)
@Composable
private fun DiscoverHeaderPreview(
    @PreviewParameter(HeaderCollapsePreviewProvider::class) collapsedFraction: Float,
) {
    val state = rememberTopAppBarState(
        initialHeightOffsetLimit = -1f,
        initialHeightOffset = -collapsedFraction,
    )
    Art_gallery_v2Theme {
        DiscoverHeader(state = state)
    }
}
