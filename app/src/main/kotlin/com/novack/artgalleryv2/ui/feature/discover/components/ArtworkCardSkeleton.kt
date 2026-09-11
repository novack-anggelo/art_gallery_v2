package com.novack.artgalleryv2.ui.feature.discover.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.novack.artgalleryv2.ui.theme.Spacing

@Composable
internal fun ArtworkCardSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "Artwork loading")
    val progress = transition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "Shimmer position",
    )
    val base = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val highlight = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
    val shimmer = Modifier.drawWithCache {
        val bandWidth = size.width * 0.6f
        onDrawBehind {
            // Read animation state during drawing, without recomposing the card each frame.
            val center = size.width * progress.value
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(base, highlight, base),
                    start = Offset(center - bandWidth, 0f),
                    end = Offset(center + bandWidth, size.height),
                ),
            )
        }
    }
    val density = LocalDensity.current
    val titleHeight = with(density) { MaterialTheme.typography.titleLarge.lineHeight.toDp() }
    val metadataHeight = with(density) { MaterialTheme.typography.bodySmall.lineHeight.toDp() }

    // The screen announces loading once; decorative skeletons have no actions or semantics.
    Card(
        modifier = modifier.clearAndSetSemantics {},
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(
            modifier = Modifier.padding(Spacing.SizeM),
            verticalArrangement = Arrangement.spacedBy(Spacing.SizeS),
        ) {
            Spacer(Modifier.fillMaxWidth().aspectRatio(1f).then(shimmer))
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.SizeXXS)) {
                Spacer(Modifier.fillMaxWidth(0.8f).height(titleHeight).then(shimmer))
                Spacer(Modifier.fillMaxWidth(0.6f).height(metadataHeight).then(shimmer))
                Spacer(Modifier.fillMaxWidth(0.4f).height(metadataHeight).then(shimmer))
            }
        }
    }
}
