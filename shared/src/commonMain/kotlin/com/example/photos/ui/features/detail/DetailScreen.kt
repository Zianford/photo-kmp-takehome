package com.example.photos.ui.features.detail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import coil3.compose.AsyncImage
import com.example.photos.domain.model.Photo

@Composable
fun DetailScreen(photo: Photo, onZoomChanged: (Boolean) -> Unit = {}) {
    var imageFailed by remember(photo.imageUrl) { mutableStateOf(false) }
    var imageLoaded by remember(photo.imageUrl) { mutableStateOf(false) }
    var scale by remember(photo.id) { mutableStateOf(1f) }
    var offset by remember(photo.id) { mutableStateOf(Offset.Zero) }
    var viewport by remember(photo.id) { mutableStateOf(IntSize.Zero) }
    val fullImageAlpha by animateFloatAsState(
        targetValue = if (imageLoaded) 1f else 0f,
        animationSpec = tween(220),
    )
    val transform = rememberTransformableState { centroid, zoomChange, panChange, _ ->
        val previousScale = scale
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        if (scale <= 1f) {
            offset = Offset.Zero
        } else {
            val center = Offset(viewport.width / 2f, viewport.height / 2f)
            val focalPoint = if (centroid == Offset.Unspecified) center else centroid
            val ratio = scale / previousScale
            val nextOffset = offset + (focalPoint - center - offset) * (1f - ratio) + panChange
            val photoWidth = photo.width.coerceAtLeast(1)
            val photoHeight = photo.height.coerceAtLeast(1)
            val fittedScale = minOf(
                viewport.width.toFloat() / photoWidth,
                viewport.height.toFloat() / photoHeight,
            )
            val maxX = ((photoWidth * fittedScale * scale) - viewport.width).coerceAtLeast(0f) / 2f
            val maxY = ((photoHeight * fittedScale * scale) - viewport.height).coerceAtLeast(0f) / 2f
            offset = Offset(
                nextOffset.x.coerceIn(-maxX, maxX),
                nextOffset.y.coerceIn(-maxY, maxY),
            )
        }
        onZoomChanged(scale > 1.01f)
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        Box(
            modifier = Modifier.fillMaxSize()
                .clipToBounds()
                .onSizeChanged { viewport = it }
                .transformable(state = transform, canPan = { scale > 1f }),
        ) {
            Box(
                modifier = Modifier.fillMaxSize().graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            ) {
                AsyncImage(
                    model = photo.thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
                AsyncImage(
                    model = photo.imageUrl,
                    contentDescription = "Photo ${photo.id}",
                    modifier = Modifier.fillMaxSize().graphicsLayer { alpha = fullImageAlpha },
                    contentScale = ContentScale.Fit,
                    onError = { imageFailed = true; imageLoaded = false },
                    onSuccess = { imageFailed = false; imageLoaded = true },
                )
            }
        }
        if (imageFailed) {
            Text("Image unavailable", modifier = Modifier.align(Alignment.Center), color = Color.White)
        }
    }
}
