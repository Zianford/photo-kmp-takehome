package com.example.photos.ui.features.uploads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.photos.domain.model.Photo
import com.example.photos.ui.designsystem.component.FeedbackPanel
import com.example.photos.ui.designsystem.component.PhotoTile
import com.example.photos.ui.designsystem.theme.PhotoSpacing

@Composable
fun UploadHistoryScreen(
    state: UploadHistoryUiState,
    onRetry: () -> Unit,
    onOpenPhoto: (Photo) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.isLoading && state.uploads.isEmpty() -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.error != null && state.uploads.isEmpty() -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            FeedbackPanel(
                title = "History unavailable",
                message = state.error,
                actionLabel = "Try again",
                onAction = onRetry,
            )
        }
        state.uploads.isEmpty() -> Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            FeedbackPanel(
                title = "No uploads yet",
                message = "Photos you upload will stay here after you reopen the app.",
            )
        }
        else -> LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 156.dp),
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = PhotoSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(PhotoSpacing.small),
            verticalArrangement = Arrangement.spacedBy(PhotoSpacing.small),
        ) {
            items(state.uploads, key = { it.fingerprint }) { upload ->
                var imageFailed by remember(upload.photo.previewUrl) { mutableStateOf(false) }
                PhotoTile(
                    onClick = { onOpenPhoto(upload.photo) },
                    caption = "Uploaded ${upload.completedAt.toString().substringBefore('T')}",
                ) {
                    AsyncImage(
                        model = upload.photo.previewUrl,
                        contentDescription = "Uploaded photo ${upload.photo.id}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        onError = { imageFailed = true },
                        onSuccess = { imageFailed = false },
                    )
                    if (imageFailed) {
                        Text(
                            "Image unavailable",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            if (state.error != null) {
                item {
                    Text(state.error, modifier = Modifier.padding(PhotoSpacing.small))
                }
            }
        }
    }
}
