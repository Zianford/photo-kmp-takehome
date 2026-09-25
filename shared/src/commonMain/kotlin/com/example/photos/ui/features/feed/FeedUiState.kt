package com.example.photos.ui.features.feed

import com.example.photos.domain.model.Photo

data class FeedUiState(
    val photos: List<Photo> = emptyList(),
    val nextCursor: String? = null,
    val isInitialLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val appendError: String? = null,
    val selectedPhoto: Photo? = null,
)

sealed interface FeedAction {
    data object Refresh : FeedAction
    data object LoadMore : FeedAction
    data object Retry : FeedAction
    data class OpenPhoto(val id: String) : FeedAction
    data object ClosePhoto : FeedAction
}
