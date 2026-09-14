package com.novack.artgalleryv2.ui.feature.discover

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.novack.artgalleryv2.ui.theme.Spacing

internal enum class DiscoverPresentation(
    val minimumCellWidth: Dp?,
    val cardPadding: Dp,
    val imageSize: Dp?,
) {
    LargeGrid(minimumCellWidth = 280.dp, cardPadding = Spacing.SizeM, imageSize = null),
    CompactGrid(minimumCellWidth = 160.dp, cardPadding = Spacing.SizeS, imageSize = null),
    ThumbnailRows(minimumCellWidth = null, cardPadding = Spacing.SizeS, imageSize = 96.dp),
}
