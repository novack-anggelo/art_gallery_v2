package com.novack.artgalleryv2.ui.feature.discover.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import com.novack.artgalleryv2.R
import com.novack.artgalleryv2.core.domain.model.ArtworkSummary
import com.novack.artgalleryv2.ui.theme.Spacing

@Composable
internal fun ArtworkCard(
    artworkSummary: ArtworkSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    metadataVisibility: ArtworkMetadataVisibility = ArtworkMetadataVisibility(),
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.SizeM),
            verticalArrangement = Arrangement.spacedBy(Spacing.SizeS),
        ) {
            AsyncImage(
                model = artworkSummary.image.url,
                contentDescription = artworkSummary.image.altText,
                contentScale = ContentScale.Fit,
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                error = painterResource(R.drawable.artwork_image_error),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
            )
            ArtworkCardFooter(
                title = artworkSummary.title,
                artist = artworkSummary.artist,
                dateDisplay = artworkSummary.dateDisplay,
                mediumDisplay = artworkSummary.mediumDisplay,
                metadataVisibility = metadataVisibility,
            )
        }
    }
}

@Composable
private fun ArtworkCardFooter(
    title: String,
    artist: String?,
    dateDisplay: String?,
    mediumDisplay: String?,
    metadataVisibility: ArtworkMetadataVisibility,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.SizeXXS),
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        artworkMetadataText(
            artist = artist.takeIf { metadataVisibility.showArtist },
            dateDisplay = dateDisplay.takeIf { metadataVisibility.showDate },
            showUnknownArtist = metadataVisibility.showArtist,
        )?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        mediumDisplay.takeIf { metadataVisibility.showMedium }?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun artworkMetadataText(
    artist: String?,
    dateDisplay: String?,
    showUnknownArtist: Boolean,
): String? {
    val artistName = artist ?: stringResource(R.string.unknown_artist).takeIf { showUnknownArtist }
    return when {
        artistName != null && dateDisplay != null ->
            stringResource(R.string.artwork_artist_and_date, artistName, dateDisplay)
        artistName != null -> artistName
        else -> dateDisplay
    }
}
