package com.example.photos.ui.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.photos.ui.designsystem.theme.PhotoSpacing

@Composable
fun FeedbackPanel(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    PhotoCard(modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(PhotoSpacing.medium),
        ) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (actionLabel != null && onAction != null) {
                PhotoActionButton(label = actionLabel, onClick = onAction)
            }
        }
    }
}
