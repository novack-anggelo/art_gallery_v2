package com.novack.artgalleryv2.feature.discover.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import com.novack.artgalleryv2.feature.discover.presentation.R
import com.novack.artgalleryv2.core.designsystem.theme.Spacing
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DiscoverHeader(
    state: TopAppBarState,
    modifier: Modifier = Modifier,
) {
    var expandedHeight by remember { mutableIntStateOf(0) }
    var compactHeight by remember { mutableIntStateOf(0) }
    val showCompactSemantics by remember(state) {
        derivedStateOf { state.collapsedFraction >= 0.5f }
    }

    // Read measured sizes during composition so onSizeChanged schedules this effect again.
    // Reading them only inside SideEffect leaves the initial, effectively unbounded limit.
    val measuredLimit = if (expandedHeight > 0 && compactHeight > 0) {
        (compactHeight - expandedHeight).coerceAtMost(0).toFloat()
    } else {
        null
    }

    SideEffect {
        measuredLimit?.let { limit ->
            if (state.heightOffsetLimit != limit) {
                // Preserve expansion when font scale or available width changes.
                val fraction = state.collapsedFraction
                state.heightOffsetLimit = limit
                state.heightOffset = limit * fraction
            }
        }
    }

    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Layout(
            modifier = Modifier
                .windowInsetsPadding(TopAppBarDefaults.windowInsets)
                .clipToBounds(),
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp)
                        .onSizeChanged { expandedHeight = it.height }
                        .graphicsLayer { alpha = 1f - state.collapsedFraction }
                        .then(if (showCompactSemantics) Modifier.clearAndSetSemantics {} else Modifier)
                        .padding(Spacing.SizeM),
                    verticalArrangement = Arrangement.spacedBy(Spacing.SizeXS, Alignment.CenterVertically),
                ) {
                    Text(
                        text = stringResource(R.string.discover_brand),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = stringResource(R.string.discover_title),
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.semantics { heading() },
                    )
                    Text(
                        text = stringResource(R.string.discover_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .onSizeChanged { compactHeight = it.height }
                        .graphicsLayer { alpha = state.collapsedFraction }
                        .then(if (!showCompactSemantics) Modifier.clearAndSetSemantics {} else Modifier)
                        .padding(horizontal = Spacing.SizeM, vertical = Spacing.SizeS),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        text = stringResource(R.string.discover_title),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { heading() },
                    )
                }
            },
        ) { measurables, constraints ->
            // Measure both complete states; only the container height collapses.
            val childConstraints = constraints.copy(minHeight = 0, maxHeight = Constraints.Infinity)
            val expanded = measurables[0].measure(childConstraints)
            val compact = measurables[1].measure(childConstraints)
            val fullHeight = maxOf(expanded.height, compact.height)
            val height = (fullHeight + state.heightOffset)
                .roundToInt()
                .coerceIn(compact.height, fullHeight)
            layout(constraints.maxWidth, constraints.constrainHeight(height)) {
                expanded.placeRelative(0, 0)
                compact.placeRelative(0, 0)
            }
        }
    }
}
