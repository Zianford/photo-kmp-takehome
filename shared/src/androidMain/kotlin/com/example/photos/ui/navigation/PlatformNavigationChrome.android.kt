package com.example.photos.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.photos.ui.designsystem.theme.PhotoSpacing

@Composable
actual fun FeedNavigationBar(
    onRefresh: () -> Unit,
    onUploadClick: () -> Unit,
    refreshEnabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(
            start = PhotoSpacing.medium,
            end = PhotoSpacing.small,
            top = PhotoSpacing.small,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "PHOTO JOURNAL",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            Text("Explore", style = MaterialTheme.typography.headlineLarge)
        }
        TextButton(onClick = onRefresh, enabled = refreshEnabled) { Text("Refresh") }
        TextButton(onClick = onUploadClick) { Text("Upload") }
    }
}

@Composable
actual fun UploadNavigationBar(onClose: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text("Uploads", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
        TextButton(onClick = onClose) { Text("Close") }
    }
}

@Composable
actual fun BoxScope.DetailNavigationClose(onClose: () -> Unit) {
    Surface(
        modifier = Modifier.align(Alignment.TopStart).safeDrawingPadding().padding(PhotoSpacing.medium),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.7f),
    ) {
        TextButton(onClick = onClose, modifier = Modifier.heightIn(min = 52.dp)) {
            Text("Close", color = Color.White)
        }
    }
}
