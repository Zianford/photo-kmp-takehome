package com.example.photos.ui.features.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.photos.domain.model.Photo
import com.example.photos.ui.designsystem.theme.PhotoSpacing

@Composable
fun DetailScreen(photo: Photo, onClose: () -> Unit) {
    var imageFailed by remember(photo.imageUrl) { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AsyncImage(
            model = photo.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
        AsyncImage(
            model = photo.imageUrl,
            contentDescription = "Photo ${photo.id}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            onError = { imageFailed = true },
            onSuccess = { imageFailed = false },
        )
        if (imageFailed) {
            Text("Image unavailable", modifier = Modifier.align(Alignment.Center), color = Color.White)
        }
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
}
