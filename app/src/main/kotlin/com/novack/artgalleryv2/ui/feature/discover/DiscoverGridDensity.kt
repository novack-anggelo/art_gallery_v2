package com.novack.artgalleryv2.ui.feature.discover

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.novack.artgalleryv2.ui.theme.Spacing

internal enum class DiscoverGridDensity(
    val minimumCellWidth: Dp,
    val cardPadding: Dp,
) {
    Comfortable(minimumCellWidth = 280.dp, cardPadding = Spacing.SizeM),
    Compact(minimumCellWidth = 160.dp, cardPadding = Spacing.SizeS),
}
