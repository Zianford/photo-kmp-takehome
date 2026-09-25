package com.example.photos

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.photos.di.appModule
import com.example.photos.ui.designsystem.showcase.DesignSystemShowcase
import com.example.photos.ui.designsystem.theme.PhotoTheme
import org.koin.compose.KoinApplication

@Composable
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        PhotoTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                DesignSystemShowcase()
            }
        }
    }
}
