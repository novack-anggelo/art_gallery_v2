package com.novack.artgalleryv2.ui.common

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.datasource.CollectionPreviewParameterProvider
import com.novack.artgalleryv2.ui.theme.Art_gallery_v2Theme
import com.novack.artgalleryv2.ui.theme.Spacing

internal data class ErrorPreviewCase(
    val title: String,
    val subtitle: String?,
    val canRetry: Boolean,
)

internal class ErrorPreviewProvider : CollectionPreviewParameterProvider<ErrorPreviewCase>(
    listOf(
        ErrorPreviewCase("Unable to load artworks", "Please try again.", canRetry = true),
        ErrorPreviewCase("Unable to load artworks", null, canRetry = true),
        ErrorPreviewCase("This content is unavailable", "Please come back later.", canRetry = false),
    )
)

@Preview(name = "Light", showBackground = true, widthDp = 360, heightDp = 640)
@Preview(
    name = "Dark",
    showBackground = true,
    widthDp = 360,
    heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Preview(name = "Large text", showBackground = true, widthDp = 360, heightDp = 640, fontScale = 1.8f)
@Composable
private fun ErrorScreenPreview(
    @PreviewParameter(ErrorPreviewProvider::class) sample: ErrorPreviewCase,
) {
    Art_gallery_v2Theme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ErrorScreen(
                title = sample.title,
                subtitle = sample.subtitle,
                onRetry = if (sample.canRetry) ({}) else null,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.SizeM),
            )
        }
    }
}
