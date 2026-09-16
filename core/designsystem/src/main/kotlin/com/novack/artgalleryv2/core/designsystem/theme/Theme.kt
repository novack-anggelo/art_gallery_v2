package com.novack.artgalleryv2.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GalleryGreen,
    onPrimary = GalleryIvory,
    primaryContainer = GalleryLightGreen,
    onPrimaryContainer = GalleryInk,
    inversePrimary = GalleryLightGreen,
    secondary = GalleryGreen,
    onSecondary = GalleryIvory,
    secondaryContainer = GalleryImageSurface,
    onSecondaryContainer = GalleryInk,
    tertiary = GalleryGreen,
    onTertiary = GalleryIvory,
    tertiaryContainer = GalleryLightGreen,
    onTertiaryContainer = GalleryInk,
    background = GalleryIvory,
    onBackground = GalleryInk,
    surface = GalleryIvory,
    onSurface = GalleryInk,
    surfaceVariant = GalleryImageSurface,
    onSurfaceVariant = GalleryMuted,
    surfaceTint = GalleryGreen,
    inverseSurface = GalleryInk,
    inverseOnSurface = GalleryLightText,
    outline = GalleryMuted,
    outlineVariant = GalleryBorder,
    surfaceBright = GalleryIvory,
    surfaceDim = Color(0xFFDEDED6),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF5F3EF),
    surfaceContainer = Color(0xFFF0EFE9),
    surfaceContainerHigh = GalleryImageSurface,
    surfaceContainerHighest = Color(0xFFE5E5DC),
)

private val DarkColorScheme = darkColorScheme(
    primary = GalleryLightGreen,
    onPrimary = GalleryInk,
    primaryContainer = GalleryGreen,
    onPrimaryContainer = GalleryLightText,
    inversePrimary = GalleryGreen,
    secondary = GalleryLightGreen,
    onSecondary = GalleryInk,
    secondaryContainer = GalleryDarkImageSurface,
    onSecondaryContainer = GalleryLightText,
    tertiary = GalleryLightGreen,
    onTertiary = GalleryInk,
    tertiaryContainer = GalleryGreen,
    onTertiaryContainer = GalleryLightText,
    background = GalleryCharcoal,
    onBackground = GalleryLightText,
    surface = GalleryCharcoal,
    onSurface = GalleryLightText,
    surfaceVariant = GalleryDarkImageSurface,
    onSurfaceVariant = GalleryDarkMuted,
    surfaceTint = GalleryLightGreen,
    inverseSurface = GalleryLightText,
    inverseOnSurface = GalleryInk,
    outline = GalleryDarkMuted,
    outlineVariant = GalleryDarkBorder,
    surfaceBright = GalleryDarkBorder,
    surfaceDim = GalleryCharcoal,
    surfaceContainerLowest = Color(0xFF121413),
    surfaceContainerLow = Color(0xFF1C201D),
    surfaceContainer = Color(0xFF222823),
    surfaceContainerHigh = GalleryDarkImageSurface,
    surfaceContainerHighest = GalleryDarkBorder,
)

@Composable
fun Art_gallery_v2Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
