package com.example.photos.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.BoxScope

@Composable
expect fun FeedNavigationBar(
    onRefresh: () -> Unit,
    onUploadClick: () -> Unit,
    refreshEnabled: Boolean,
)

@Composable
expect fun UploadNavigationBar(onClose: () -> Unit)

@Composable
expect fun BoxScope.DetailNavigationClose(onClose: () -> Unit)
