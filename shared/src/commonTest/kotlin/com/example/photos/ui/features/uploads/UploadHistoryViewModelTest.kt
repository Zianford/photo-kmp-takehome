package com.example.photos.ui.features.uploads

import com.example.photos.domain.model.CompletedUpload
import com.example.photos.domain.model.Photo
import com.example.photos.domain.model.UploadRecord
import com.example.photos.domain.repository.UploadHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class UploadHistoryViewModelTest {
    @Test
    fun retryRestoresPersistedUploadsAfterReadFailure() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = RecoveringHistory()
            val viewModel = UploadHistoryViewModel(repository)
            advanceUntilIdle()
            assertEquals("Couldn't load upload history.", viewModel.state.value.error)

            viewModel.retry()
            advanceUntilIdle()
            assertEquals(listOf("uploaded"), viewModel.state.value.uploads.map { it.photo.id })
            assertNull(viewModel.state.value.error)

            val reopened = UploadHistoryViewModel(repository)
            advanceUntilIdle()
            assertEquals(listOf("uploaded"), reopened.state.value.uploads.map { it.photo.id })
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class RecoveringHistory : UploadHistoryRepository {
        private var reads = 0

        override fun observeCompleted(): Flow<List<CompletedUpload>> = flow {
            reads++
            if (reads == 1) error("database temporarily unavailable")
            emit(listOf(CompletedUpload(
                fingerprint = "hash",
                clientUploadId = "stable-id",
                photo = Photo(
                    id = "uploaded", width = 100, height = 100,
                    createdAt = Instant.fromEpochMilliseconds(1_000),
                    thumbnailUrl = "thumbnail", previewUrl = "preview", imageUrl = "image",
                ),
                completedAt = Instant.fromEpochMilliseconds(2_000),
            )))
        }

        override suspend fun reserve(fingerprint: String, proposedClientUploadId: String): UploadRecord =
            error("Not used by history")

        override suspend fun complete(fingerprint: String, photo: Photo) {
            error("Not used by history")
        }
    }
}
