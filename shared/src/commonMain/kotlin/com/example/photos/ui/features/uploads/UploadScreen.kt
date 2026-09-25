package com.example.photos.ui.features.uploads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.photos.domain.model.Photo
import com.example.photos.ui.designsystem.component.PhotoActionButton
import com.example.photos.ui.designsystem.component.PhotoButtonStyle
import com.example.photos.ui.designsystem.component.UploadProgressRow
import com.example.photos.ui.designsystem.theme.PhotoSpacing
import com.example.photos.ui.navigation.UploadNavigationBar

@Composable
fun UploadScreen(
    state: UploadUiState,
    history: UploadHistoryUiState,
    onAction: (UploadAction) -> Unit,
    onHistoryRetry: () -> Unit,
    onOpenHistoryPhoto: (Photo) -> Unit,
    onClose: () -> Unit,
) {
    var showHistory by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(PhotoSpacing.medium),
        verticalArrangement = Arrangement.spacedBy(PhotoSpacing.medium),
    ) {
        UploadNavigationBar(onClose = onClose)
        Row(horizontalArrangement = Arrangement.spacedBy(PhotoSpacing.small)) {
            PhotoActionButton(
                label = "Current",
                onClick = { showHistory = false },
                modifier = Modifier.weight(1f),
                style = if (showHistory) PhotoButtonStyle.Secondary else PhotoButtonStyle.Primary,
            )
            PhotoActionButton(
                label = "History",
                onClick = { showHistory = true },
                modifier = Modifier.weight(1f),
                style = if (showHistory) PhotoButtonStyle.Primary else PhotoButtonStyle.Secondary,
            )
        }
        if (showHistory) {
            UploadHistoryScreen(
                state = history,
                onRetry = onHistoryRetry,
                onOpenPhoto = onOpenHistoryPhoto,
                modifier = Modifier.weight(1f),
            )
        } else {
            Text("Choose photos to add to your journal.", style = MaterialTheme.typography.bodyLarge)
            PhotoActionButton(
                label = if (state.isPicking) "Opening photos…" else "Select photos",
                onClick = { onAction(UploadAction.Pick) },
                enabled = !state.isPicking,
            )
            state.pickerError?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = { onAction(UploadAction.DismissError) }) { Text("Dismiss") }
            }
            if (state.items.isEmpty()) {
                Text("Your selected photos will appear here with upload progress.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(PhotoSpacing.small)) {
                    items(state.items, key = { it.id }) { item ->
                        UploadProgressRow(
                            title = item.name,
                            status = item.status.label(item.error),
                            progress = item.progress,
                            onRetry = if (item.status == UploadStatus.Failed && item.retryable) {
                                { onAction(UploadAction.Retry(item.id)) }
                            } else null,
                        )
                    }
                }
            }
        }
    }
}

private fun UploadStatus.label(error: String?): String = when (this) {
    UploadStatus.Queued -> "Waiting to upload"
    UploadStatus.Uploading -> "Uploading"
    UploadStatus.Completed -> "Uploaded"
    UploadStatus.AlreadyUploaded -> "Already uploaded"
    UploadStatus.Failed -> error ?: "Upload failed"
}
