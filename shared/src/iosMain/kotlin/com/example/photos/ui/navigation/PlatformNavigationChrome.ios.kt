package com.example.photos.ui.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
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

private val IosBlue = Color(0xFF007AFF)
private val IosBlueDark = Color(0xFF0A84FF)

@Composable
private fun actionColor(): Color = if (isSystemInDarkTheme()) IosBlueDark else IosBlue

@Composable
actual fun FeedNavigationBar(
    onRefresh: () -> Unit,
    onUploadClick: () -> Unit,
    refreshEnabled: Boolean,
) {
    val tint = actionColor()
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = PhotoSpacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            TextButton(onClick = onRefresh, enabled = refreshEnabled) { Text("Refresh", color = tint) }
        }
        Text("Photo Journal", style = MaterialTheme.typography.titleMedium)
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = onUploadClick) { Text("Add", color = tint) }
        }
    }
    Text(
        "Photos",
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier.padding(start = PhotoSpacing.medium, top = PhotoSpacing.small),
    )
}

@Composable
actual fun UploadNavigationBar(onClose: () -> Unit) {
    val tint = actionColor()
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.weight(1f))
        Text("Uploads", style = MaterialTheme.typography.titleMedium)
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = onClose) { Text("Done", color = tint) }
        }
    }
}

@Composable
actual fun BoxScope.DetailNavigationClose(onClose: () -> Unit) {
    Surface(
        modifier = Modifier.align(Alignment.TopEnd).safeDrawingPadding().padding(PhotoSpacing.medium),
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.7f),
    ) {
        TextButton(onClick = onClose, modifier = Modifier.heightIn(min = 52.dp)) {
            Text("Done", color = Color.White)
        }
    }
}
