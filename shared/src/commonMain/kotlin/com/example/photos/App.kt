package com.example.photos

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationEventHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import com.example.photos.di.appModule
import com.example.photos.domain.model.Photo
import com.example.photos.ui.designsystem.theme.PhotoTheme
import com.example.photos.ui.features.detail.DetailSystemBars
import com.example.photos.ui.features.detail.PhotoDetailPager
import com.example.photos.ui.features.feed.FeedAction
import com.example.photos.ui.features.feed.FeedScreen
import com.example.photos.ui.features.feed.FeedViewModel
import com.example.photos.ui.features.uploads.UploadHistoryViewModel
import com.example.photos.ui.features.uploads.UploadScreen
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
            var openedFeedPhotoId by remember { mutableStateOf<String?>(null) }
            val feedDetailVisibility = remember { MutableTransitionState(false) }
            feedDetailVisibility.targetState = state.selectedPhoto != null
            val completedUploads = uploadState.items.count { it.status == UploadStatus.Completed }
            LaunchedEffect(completedUploads) {
                if (completedUploads > 0) viewModel.onAction(FeedAction.Refresh)
            }
            DetailSystemBars(
                active = feedDetailVisibility.currentState || feedDetailVisibility.targetState || historyPhoto != null,
            )
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
                SharedTransitionLayout {
                    Box {
                        FeedScreen(
                            state = state,
                            onAction = { action ->
                                if (action is FeedAction.OpenPhoto) {
                                    openedFeedPhotoId = action.id
                                }
                                viewModel.onAction(action)
                            },
                            onUploadClick = { showUploads = true },
                            selectedPhotoId = state.selectedPhoto?.id,
                            sharedTransitionScope = this@SharedTransitionLayout,
                        )
                        AnimatedVisibility(
                            visibleState = feedDetailVisibility,
                            enter = fadeIn(),
                            exit = fadeOut(),
                        ) {
                            openedFeedPhotoId?.let { photoId ->
                                key(photoId) {
                                    PhotoDetailPager(
                                        photos = state.photos,
                                        initialPhotoId = photoId,
                                        onClose = { viewModel.onAction(FeedAction.ClosePhoto) },
                                        onNearEnd = { viewModel.onAction(FeedAction.LoadMore) },
                                        onPageChanged = { id ->
                                            if (state.selectedPhoto != null) {
                                                viewModel.onAction(FeedAction.OpenPhoto(id))
                                            }
                                        },
                                        sharedTransitionScope = this@SharedTransitionLayout,
                                        animatedVisibilityScope = this@AnimatedVisibility,
                                    )
                                }
                            }
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
}
