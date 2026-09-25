package com.example.photos.ui.features.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photos.domain.repository.PhotoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModel(private val repository: PhotoRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(FeedUiState())
    val state = mutableState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load(reset = true)
    }

    fun onAction(action: FeedAction) {
        when (action) {
            FeedAction.Refresh -> load(reset = true)
            FeedAction.LoadMore -> load(reset = false)
            FeedAction.Retry -> load(reset = state.value.photos.isEmpty() || state.value.appendError == null)
            is FeedAction.OpenPhoto -> mutableState.update { current ->
                current.copy(selectedPhoto = current.photos.firstOrNull { it.id == action.id })
            }
            FeedAction.ClosePhoto -> mutableState.update { it.copy(selectedPhoto = null) }
        }
    }

    private fun load(reset: Boolean) {
        if (loadJob?.isActive == true) return

        val cursor = if (reset) null else (state.value.nextCursor ?: return)
        mutableState.update { current ->
            current.copy(
                isInitialLoading = reset && current.photos.isEmpty(),
                isRefreshing = reset && current.photos.isNotEmpty(),
                isLoadingMore = !reset,
                error = if (reset) null else current.error,
                appendError = null,
            )
        }

        loadJob = viewModelScope.launch {
            try {
                val page = repository.feed(cursor)
                mutableState.update { current ->
                    val photos = if (reset) page.photos else {
                        val existingIds = current.photos.mapTo(mutableSetOf()) { it.id }
                        current.photos + page.photos.filter { existingIds.add(it.id) }
                    }
                    current.copy(
                        photos = photos,
                        nextCursor = page.nextCursor,
                        isInitialLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                    )
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                mutableState.update { current ->
                    current.copy(
                        isInitialLoading = false,
                        isRefreshing = false,
                        isLoadingMore = false,
                        error = if (reset) "Couldn't load photos. Please try again." else current.error,
                        appendError = if (reset) null else "Couldn't load more photos.",
                    )
                }
            }
        }
    }
}
