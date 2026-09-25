package com.example.photos.ui.features.uploads

import com.example.photos.data.repository.Sha256ContentHasher
import com.example.photos.domain.model.CompletedUpload
import com.example.photos.domain.model.Photo
import com.example.photos.domain.model.PhotoUploadEvent
import com.example.photos.domain.model.PickedPhoto
import com.example.photos.domain.model.UploadFailure
import com.example.photos.domain.model.UploadRecord
import com.example.photos.domain.repository.PhotoPicker
import com.example.photos.domain.repository.PhotoUploadRepository
import com.example.photos.domain.repository.UploadHistoryRepository
import com.example.photos.domain.usecase.PrepareUploadUseCase
import com.example.photos.domain.usecase.UploadIdFactory
import com.example.photos.domain.usecase.UploadPhotoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class UploadViewModelTest {
    @Test
    fun duplicateSelectionIsOneRowAndRetryKeepsIt() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val history = MemoryHistory()
            val remote = FlakyUploadRepository()
            val upload = UploadPhotoUseCase(
                PrepareUploadUseCase(
                    history,
                    Sha256ContentHasher(),
                    UploadIdFactory { "stable-id" },
                    StandardTestDispatcher(testScheduler),
                ),
                remote,
                history,
            )
            val picker = PhotoPicker {
                listOf(PickedPhoto("first.jpg", "same".encodeToByteArray()),
                    PickedPhoto("copy.jpg", "same".encodeToByteArray()))
            }
            val viewModel = UploadViewModel(picker, upload)

            viewModel.onAction(UploadAction.Pick)
            advanceUntilIdle()
            val failed = viewModel.state.value.items.single()
            assertEquals(UploadStatus.Failed, failed.status)
            assertEquals(0.5f, failed.progress)
            assertFalse(viewModel.state.value.isPicking)

            viewModel.onAction(UploadAction.Retry(failed.id))
            advanceUntilIdle()
            assertEquals(UploadStatus.Completed, viewModel.state.value.items.single().status)
            assertEquals(listOf("stable-id", "stable-id"), remote.ids)
        } finally {
            Dispatchers.resetMain()
        }
    }

    private class FlakyUploadRepository : PhotoUploadRepository {
        val ids = mutableListOf<String>()

        override fun upload(clientUploadId: String, bytes: ByteArray): Flow<PhotoUploadEvent> = flow {
            ids += clientUploadId
            emit(PhotoUploadEvent.Progress(0.5f))
            if (ids.size == 1) throw UploadFailure("Connection reset", retryable = true)
            emit(PhotoUploadEvent.Completed(Photo(
                id = "uploaded", width = 100, height = 100,
                createdAt = Instant.fromEpochMilliseconds(1_000),
                thumbnailUrl = "thumbnail", previewUrl = "preview", imageUrl = "image",
            )))
        }
    }

    private class MemoryHistory : UploadHistoryRepository {
        private val records = mutableMapOf<String, UploadRecord>()

        override suspend fun reserve(fingerprint: String, proposedClientUploadId: String): UploadRecord =
            records.getOrPut(fingerprint) { UploadRecord(fingerprint, proposedClientUploadId, null) }

        override suspend fun complete(fingerprint: String, photo: Photo) {
            records[fingerprint] = requireNotNull(records[fingerprint]).copy(photo = photo)
        }

        override fun observeCompleted(): Flow<List<CompletedUpload>> = flowOf(emptyList())
    }
}
