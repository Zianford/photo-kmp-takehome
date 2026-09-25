package com.example.photos

import androidx.compose.ui.window.ComposeUIViewController
import com.example.photos.di.platformModule

fun MainViewController() = platformModule().let { dependencies ->
    ComposeUIViewController { App(dependencies) }
}
