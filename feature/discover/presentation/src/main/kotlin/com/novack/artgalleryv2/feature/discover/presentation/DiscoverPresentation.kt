package com.novack.artgalleryv2.feature.discover.presentation

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.novack.artgalleryv2.feature.discover.domain.model.DiscoverPresentation
import com.novack.artgalleryv2.core.designsystem.theme.Spacing

internal val DiscoverPresentation.minimumCellWidth: Dp?
    get() = when (this) {
        DiscoverPresentation.LargeGrid -> 280.dp
        DiscoverPresentation.CompactGrid -> 160.dp
        DiscoverPresentation.ThumbnailRows -> null
    }

internal val DiscoverPresentation.cardPadding: Dp
    get() = when (this) {
        DiscoverPresentation.LargeGrid -> Spacing.SizeM
        DiscoverPresentation.CompactGrid,
        DiscoverPresentation.ThumbnailRows -> Spacing.SizeS
    }

internal val DiscoverPresentation.imageSize: Dp?
    get() = when (this) {
        DiscoverPresentation.LargeGrid,
        DiscoverPresentation.CompactGrid -> null
        DiscoverPresentation.ThumbnailRows -> 96.dp
    }
