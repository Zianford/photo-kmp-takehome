package com.example.photos

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.photos.di.appModule
import com.example.photos.ui.designsystem.theme.PhotoTheme
import com.example.photos.ui.features.detail.DetailScreen
import com.example.photos.ui.features.detail.DetailSystemBars
import com.example.photos.ui.features.feed.FeedAction
import com.example.photos.ui.features.feed.FeedScreen
import com.example.photos.ui.features.feed.FeedViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        PhotoTheme {
            val viewModel = koinViewModel<FeedViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            DetailSystemBars(active = state.selectedPhoto != null)
            val navigationState = rememberNavigationEventState(NavigationEventInfo.None)
            NavigationEventHandler(
                state = navigationState,
                isForwardEnabled = false,
                isBackEnabled = state.selectedPhoto != null,
                onBackCompleted = { viewModel.onAction(FeedAction.ClosePhoto) },
            )
            Surface(modifier = Modifier.fillMaxSize()) {
                Box {
                    FeedScreen(state = state, onAction = viewModel::onAction)
                    state.selectedPhoto?.let { photo ->
                        DetailScreen(photo = photo, onClose = { viewModel.onAction(FeedAction.ClosePhoto) })
                    }
                }
            }
        }
    }
}
