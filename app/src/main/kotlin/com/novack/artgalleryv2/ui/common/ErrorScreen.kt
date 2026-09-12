package com.novack.artgalleryv2.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.novack.artgalleryv2.R
import com.novack.artgalleryv2.ui.theme.Spacing

@Composable
internal fun ErrorScreen(
    title: String,
    onRetry: (() -> Unit)?,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        subtitle?.let {
            Spacer(modifier = Modifier.height(Spacing.SizeS))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        onRetry?.let {
            Spacer(modifier = Modifier.height(Spacing.SizeM))
            Button(onClick = it) {
                Text(text = stringResource(R.string.retry))
            }
        }
    }
}
