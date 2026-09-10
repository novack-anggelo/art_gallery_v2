package com.novack.artgalleryv2.core.data.mappers

import com.novack.artgalleryv2.core.data.remote.model.ArtworkPageDto
import com.novack.artgalleryv2.core.data.remote.model.ArtworkSummaryDto
import com.novack.artgalleryv2.core.data.remote.model.ArtworkThumbnailDto
import com.novack.artgalleryv2.core.domain.model.ArtworkImage
import com.novack.artgalleryv2.core.domain.model.ArtworkSummary

private const val OVERVIEW_IMAGE_WIDTH = 843

internal fun ArtworkPageDto.toDomain(): List<ArtworkSummary> =
    data.mapNotNull { artwork ->
        artwork.toDomainOrNull(iiifBaseUrl = config.iiifUrl)
    }

internal fun ArtworkSummaryDto.toDomainOrNull(
    iiifBaseUrl: String
): ArtworkSummary? {
    val image = toArtworkImage(iiifBaseUrl) ?: return null

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
    iiifBaseUrl: String
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
    )
}

private fun ArtworkThumbnailDto?.toAspectRatio(): Float? {
    val validWidth = this?.width?.takeIf { it > 0 }
        ?: return null
    val validHeight = height?.takeIf { it > 0 }
        ?: return null

    return validWidth.toFloat() / validHeight.toFloat()
}