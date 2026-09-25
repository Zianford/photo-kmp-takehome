package com.example.photos.ui.features.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.photos.domain.model.Photo
import com.example.photos.ui.designsystem.component.FeedbackPanel
import com.example.photos.ui.designsystem.component.PhotoTile
import com.example.photos.ui.designsystem.theme.PhotoSpacing
import com.example.photos.ui.navigation.FeedNavigationBar
import com.example.photos.ui.navigation.SharedPhotoKey
import kotlinx.coroutines.flow.first

@Composable
fun FeedScreen(
    state: FeedUiState,
    onAction: (FeedAction) -> Unit,
    onUploadClick: () -> Unit = {},
    selectedPhotoId: String? = null,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    val gridState = rememberLazyGridState()

    LaunchedEffect(gridState, state.photos.size, state.nextCursor) {
        if (state.photos.isNotEmpty() && state.nextCursor != null && state.appendError == null) {
            snapshotFlow {
                val visibleLast = gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
                visibleLast >= gridState.layoutInfo.totalItemsCount - 4
            }.first { it }
            onAction(FeedAction.LoadMore)
        }
    }

    Column(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        FeedNavigationBar(
            onRefresh = { onAction(FeedAction.Refresh) },
            onUploadClick = onUploadClick,
            refreshEnabled = !state.isRefreshing,
        )

        when {
            state.isInitialLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.photos.isEmpty() -> Box(Modifier.fillMaxSize().padding(PhotoSpacing.medium)) {
                FeedbackPanel(
                    title = if (state.error == null) "No photos yet" else "Couldn't load photos",
                    message = state.error ?: "Pull up a chair. New moments will appear here.",
                    actionLabel = if (state.error == null) "Refresh" else "Try again",
                    onAction = { onAction(FeedAction.Retry) },
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 156.dp),
                state = gridState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(PhotoSpacing.medium),
                horizontalArrangement = Arrangement.spacedBy(PhotoSpacing.small),
                verticalArrangement = Arrangement.spacedBy(PhotoSpacing.small),
            ) {
                items(state.photos, key = { it.id }) { photo ->
                    FeedPhotoTile(
                        photo = photo,
                        onClick = { onAction(FeedAction.OpenPhoto(photo.id)) },
                        selected = selectedPhotoId == photo.id,
                        sharedTransitionScope = sharedTransitionScope,
                    )
                }
                if (state.isRefreshing || state.isLoadingMore) {
                    item(span = { GridItemSpan(maxLineSpan) }) { LoadingContent() }
                }
                if (state.appendError != null || state.error != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        FeedbackPanel(
                            title = if (state.appendError != null) "More photos unavailable" else "Refresh failed",
                            message = state.appendError ?: state.error.orEmpty(),
                            actionLabel = "Try again",
                            onAction = { onAction(FeedAction.Retry) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxWidth().padding(PhotoSpacing.extraLarge), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FeedPhotoTile(
    photo: Photo,
    onClick: () -> Unit,
    selected: Boolean,
    sharedTransitionScope: SharedTransitionScope?,
) {
    var imageFailed by remember(photo.previewUrl) { mutableStateOf(false) }

    PhotoTile(onClick = onClick) {
        AnimatedVisibility(visible = !selected, modifier = Modifier.fillMaxSize()) {
            val imageModifier = if (sharedTransitionScope != null) {
                with(sharedTransitionScope) {
                    Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState(SharedPhotoKey(photo.id)),
                        animatedVisibilityScope = this@AnimatedVisibility,
                    )
                }
            } else {
                Modifier
            }
            AsyncImage(
                model = photo.previewUrl,
                contentDescription = "Open photo ${photo.id}",
                contentScale = ContentScale.Crop,
                modifier = imageModifier.fillMaxSize(),
                onError = { imageFailed = true },
                onSuccess = { imageFailed = false },
            )
        }
        if (imageFailed) {
            Text(
                "Image unavailable",
                modifier = Modifier.align(Alignment.Center).padding(PhotoSpacing.small),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
