package com.example.photos.ui.designsystem.showcase

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.photos.ui.designsystem.component.FeedbackPanel
import com.example.photos.ui.designsystem.component.PhotoActionButton
import com.example.photos.ui.designsystem.component.PhotoButtonStyle
import com.example.photos.ui.designsystem.component.PhotoCard
import com.example.photos.ui.designsystem.component.PhotoTile
import com.example.photos.ui.designsystem.component.UploadProgressRow
import com.example.photos.ui.designsystem.theme.PhotoSpacing
import com.example.photos.ui.designsystem.theme.PhotoTheme

@Composable
fun DesignSystemShowcase() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(PhotoSpacing.large),
        verticalArrangement = Arrangement.spacedBy(PhotoSpacing.large),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(PhotoSpacing.small)) {
            Text("PHOTO JOURNAL", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Text("Moments worth keeping", style = MaterialTheme.typography.headlineLarge)
            Text(
                "A small set of shared building blocks for the feed and your uploads.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        PhotoTile(
            onClick = {},
            aspectRatio = 1.35f,
            caption = "A quiet afternoon",
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF899B89), Color(0xFFD8B08C), Color(0xFF725D55)),
                        ),
                    ),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(PhotoSpacing.small)) {
            PhotoActionButton("Upload photos", onClick = {}, modifier = Modifier.weight(1f))
            PhotoActionButton(
                "View history",
                onClick = {},
                modifier = Modifier.weight(1f),
                style = PhotoButtonStyle.Secondary,
            )
        }
        PhotoCard {
            Text("Your collection", style = MaterialTheme.typography.titleMedium)
            Text(
                "New photos will appear here after you choose them.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        UploadProgressRow(title = "coastline.jpg", status = "Uploading · 64%", progress = 0.64f)
        UploadProgressRow(title = "portrait.jpg", status = "Upload failed", progress = 0.36f, onRetry = {})
        FeedbackPanel(
            title = "Couldn't load photos",
            message = "Check your connection and try again.",
            actionLabel = "Try again",
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun DesignSystemShowcasePreview() {
    PhotoTheme { DesignSystemShowcase() }
}
