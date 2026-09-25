package com.example.photos.ui.features.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import platform.Foundation.NSNotificationCenter

@Composable
actual fun DetailSystemBars(active: Boolean) {
    LaunchedEffect(active) {
        NSNotificationCenter.defaultCenter.postNotificationName(
            aName = if (active) "PhotoDetailDidOpen" else "PhotoDetailDidClose",
            `object` = null,
        )
    }
}
