package com.novack.artgalleryv2.feature.discover.presentation.components

import coil3.network.NetworkHeaders
import com.novack.artgalleryv2.core.artwork.domain.model.ArtworkImage

private const val REFERER_HEADER = "Referer"

internal fun ArtworkImage.networkHeaders(): NetworkHeaders =
    NetworkHeaders.Builder().apply {
        sourceWebsiteUrl?.let { set(REFERER_HEADER, it) }
    }.build()
