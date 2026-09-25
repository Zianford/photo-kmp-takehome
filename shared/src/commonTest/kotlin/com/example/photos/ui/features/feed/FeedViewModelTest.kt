package com.example.photos.ui.features.feed

import com.example.photos.domain.model.Photo
import com.example.photos.domain.model.PhotoPage
import com.example.photos.domain.repository.PhotoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock

@OptIn(ExperimentalCoroutinesApi::class)
class FeedViewModelTest {
    @Test
    fun appendFailureKeepsPhotosAndRetriesSameCursor() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = SequencedRepository()
            val viewModel = FeedViewModel(repository)
            advanceUntilIdle()

            assertEquals(listOf("first"), viewModel.state.value.photos.map { it.id })
            viewModel.onAction(FeedAction.LoadMore)
            advanceUntilIdle()

            assertEquals(listOf("first"), viewModel.state.value.photos.map { it.id })
            assertEquals("first", viewModel.state.value.nextCursor)
            assertNotNull(viewModel.state.value.appendError)

            viewModel.onAction(FeedAction.Retry)
            advanceUntilIdle()

            assertEquals(listOf(null, "first", "first"), repository.requestedCursors)
            assertEquals(listOf("first", "second"), viewModel.state.value.photos.map { it.id })
            assertNull(viewModel.state.value.appendError)
            assertNull(viewModel.state.value.nextCursor)
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun refreshFailureKeepsVisibleContent() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = SequencedRepository()
            val viewModel = FeedViewModel(repository)
            advanceUntilIdle()

            repository.failRefresh = true
            viewModel.onAction(FeedAction.Refresh)
            advanceUntilIdle()

            assertEquals(listOf("first"), viewModel.state.value.photos.map { it.id })
            assertNotNull(viewModel.state.value.error)
            assertFalse(viewModel.state.value.isRefreshing)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class SequencedRepository : PhotoRepository {
        val requestedCursors = mutableListOf<String?>()
        var failRefresh = false
        private var appendAttempts = 0

        override suspend fun feed(cursor: String?, pageSize: Int): PhotoPage {
            requestedCursors += cursor
            if (cursor == null) {
                if (failRefresh) error("offline")
                return PhotoPage(listOf(photo("first")), nextCursor = "first")
            }
            appendAttempts++
            if (appendAttempts == 1) error("offline")
            return PhotoPage(listOf(photo("first"), photo("second")), nextCursor = null)
        }

        private fun photo(id: String) = Photo(
            id = id,
            width = 100,
            height = 100,
            createdAt = Clock.System.now(),
            thumbnailUrl = "thumbnail/$id",
            previewUrl = "preview/$id",
            imageUrl = "image/$id",
        )
    }
}
