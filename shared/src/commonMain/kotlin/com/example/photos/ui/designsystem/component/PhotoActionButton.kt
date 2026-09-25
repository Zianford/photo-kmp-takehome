package com.example.photos.ui.designsystem.component

import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

enum class PhotoButtonStyle { Primary, Secondary }

@Composable
fun PhotoActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    style: PhotoButtonStyle = PhotoButtonStyle.Primary,
) {
    val buttonModifier = modifier.heightIn(min = 52.dp)
    when (style) {
        PhotoButtonStyle.Primary -> Button(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
        ) { Text(label) }
        PhotoButtonStyle.Secondary -> OutlinedButton(
            onClick = onClick,
            modifier = buttonModifier,
            enabled = enabled,
        ) { Text(label) }
    }
}
