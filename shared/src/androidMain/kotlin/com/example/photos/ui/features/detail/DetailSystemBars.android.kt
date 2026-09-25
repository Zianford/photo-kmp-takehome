package com.example.photos.ui.features.detail

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun DetailSystemBars(active: Boolean) {
    val activity = LocalContext.current as? Activity
    val view = LocalView.current

    if (active && activity != null) {
        DisposableEffect(activity, view) {
            val controller = WindowCompat.getInsetsController(activity.window, view)
            val previous = controller.isAppearanceLightStatusBars
            controller.isAppearanceLightStatusBars = false
            onDispose { controller.isAppearanceLightStatusBars = previous }
        }
    }
}
