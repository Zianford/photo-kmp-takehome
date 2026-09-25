package com.example.photos.ui.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun PhotoTile(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 1f,
    caption: String? = null,
    image: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
    ) {
        image()
        if (caption != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f)),
                        ),
                    )
                    .padding(top = 28.dp, start = 16.dp, end = 16.dp, bottom = 14.dp),
            ) {
                Text(caption, color = Color.White, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
