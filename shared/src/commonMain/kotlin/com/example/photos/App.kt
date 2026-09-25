package com.example.photos

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.photos.di.appModule
import com.example.photos.domain.model.Photo
import com.example.photos.ui.designsystem.theme.PhotoTheme
import com.example.photos.ui.features.detail.PhotoDetailPager
import com.example.photos.ui.features.detail.DetailSystemBars
import com.example.photos.ui.features.feed.FeedAction
import com.example.photos.ui.features.feed.FeedScreen
import com.example.photos.ui.features.feed.FeedViewModel
import com.example.photos.ui.features.uploads.UploadScreen
import com.example.photos.ui.features.uploads.UploadHistoryViewModel
import com.example.photos.ui.features.uploads.UploadStatus
import com.example.photos.ui.features.uploads.UploadViewModel
import org.koin.compose.KoinApplication
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.module.Module

@Composable
fun App(platformModule: Module) {
    KoinApplication(application = { modules(appModule, platformModule) }) {
        PhotoTheme {
            val viewModel = koinViewModel<FeedViewModel>()
            val state by viewModel.state.collectAsStateWithLifecycle()
            val uploadViewModel = koinViewModel<UploadViewModel>()
            val uploadState by uploadViewModel.state.collectAsStateWithLifecycle()
            val historyViewModel = koinViewModel<UploadHistoryViewModel>()
            val historyState by historyViewModel.state.collectAsStateWithLifecycle()
            var showUploads by remember { mutableStateOf(false) }
            var historyPhoto by remember { mutableStateOf<Photo?>(null) }
            val completedUploads = uploadState.items.count { it.status == UploadStatus.Completed }
            LaunchedEffect(completedUploads) {
                if (completedUploads > 0) viewModel.onAction(FeedAction.Refresh)
            }
            DetailSystemBars(active = state.selectedPhoto != null || historyPhoto != null)
            val navigationState = rememberNavigationEventState(NavigationEventInfo.None)
            NavigationEventHandler(
                state = navigationState,
                isForwardEnabled = false,
                isBackEnabled = state.selectedPhoto != null || showUploads || historyPhoto != null,
                onBackCompleted = {
                    when {
                        historyPhoto != null -> historyPhoto = null
                        showUploads -> showUploads = false
                        else -> viewModel.onAction(FeedAction.ClosePhoto)
                    }
                },
            )
            Surface(modifier = Modifier.fillMaxSize()) {
                Box {
                    FeedScreen(state = state, onAction = viewModel::onAction, onUploadClick = { showUploads = true })
                    state.selectedPhoto?.let { photo ->
                        PhotoDetailPager(
                            photos = state.photos,
                            initialPhotoId = photo.id,
                            onClose = { viewModel.onAction(FeedAction.ClosePhoto) },
                            onNearEnd = { viewModel.onAction(FeedAction.LoadMore) },
                        )
                    }
                    if (showUploads) {
                        Surface(modifier = Modifier.fillMaxSize()) {
                            UploadScreen(
                                state = uploadState,
                                history = historyState,
                                onAction = uploadViewModel::onAction,
                                onHistoryRetry = historyViewModel::retry,
                                onOpenHistoryPhoto = { historyPhoto = it },
                                onClose = { showUploads = false },
                            )
                        }
                    }
                    historyPhoto?.let { photo ->
                        PhotoDetailPager(
                            photos = historyState.uploads.map { it.photo },
                            initialPhotoId = photo.id,
                            onClose = { historyPhoto = null },
                        )
                    }
                }
            }
        }
    }
}
