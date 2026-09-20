package com.novack.artgalleryv2.core.artwork.data.mapper

import com.novack.artgalleryv2.core.artwork.data.remote.model.ArtworkPageDto
import com.novack.artgalleryv2.core.artwork.data.remote.model.ArtworkSummaryDto
import com.novack.artgalleryv2.core.artwork.data.remote.model.ArtworkThumbnailDto
import com.novack.artgalleryv2.core.artwork.domain.model.ArtworkImage
import com.novack.artgalleryv2.core.artwork.domain.model.ArtworkSummary

private const val OVERVIEW_IMAGE_WIDTH = 843

internal fun ArtworkPageDto.toDomain(): List<ArtworkSummary> =
    data.mapNotNull { artwork ->
        artwork.toDomainOrNull(
            iiifBaseUrl = config.iiifUrl,
            sourceWebsiteUrl = config.websiteUrl,
        )
    }

internal fun ArtworkSummaryDto.toDomainOrNull(
    iiifBaseUrl: String,
    sourceWebsiteUrl: String? = null,
): ArtworkSummary? {
    val image = toArtworkImage(iiifBaseUrl, sourceWebsiteUrl) ?: return null

    return ArtworkSummary(
        id = id,
        title = title,
        artist = artistTitle?.takeIf(String::isNotBlank),
        dateDisplay = dateDisplay?.takeIf(String::isNotBlank),
        mediumDisplay = mediumDisplay?.takeIf(String::isNotBlank),
        image = image,
        isPublicDomain = isPublicDomain == true
    )
}

private fun ArtworkSummaryDto.toArtworkImage(
    iiifBaseUrl: String,
    sourceWebsiteUrl: String?,
): ArtworkImage? {
    val validImageId = imageId?.takeIf(String::isNotBlank)
        ?: return null
    val validBaseUrl = iiifBaseUrl.takeIf(String::isNotBlank)
        ?: return null

    return ArtworkImage(
        url = buildString {
            append(validBaseUrl.trimEnd('/'))
            append('/')
            append(validImageId)
            append("/full/")
            append(OVERVIEW_IMAGE_WIDTH)
            append(",/0/default.jpg")
        },
        altText = thumbnail?.altText?.takeIf(String::isNotBlank),
        aspectRatio = thumbnail.toAspectRatio(),
        sourceWebsiteUrl = sourceWebsiteUrl?.takeIf(String::isNotBlank),
    )
}

private fun ArtworkThumbnailDto?.toAspectRatio(): Float? {
    val validWidth = this?.width?.takeIf { it > 0 }
        ?: return null
    val validHeight = height?.takeIf { it > 0 }
        ?: return null

    return validWidth.toFloat() / validHeight.toFloat()
}
