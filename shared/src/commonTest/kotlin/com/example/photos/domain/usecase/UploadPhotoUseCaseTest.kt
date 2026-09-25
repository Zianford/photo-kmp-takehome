package com.example.photos.domain.usecase

import com.example.photos.data.repository.Sha256ContentHasher
import com.example.photos.domain.model.CompletedUpload
import com.example.photos.domain.model.Photo
import com.example.photos.domain.model.PhotoUploadEvent
import com.example.photos.domain.model.UploadRecord
import com.example.photos.domain.model.UploadFailure
import com.example.photos.domain.repository.PhotoUploadRepository
import com.example.photos.domain.repository.UploadHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.time.Instant

class UploadPhotoUseCaseTest {
    @Test
    fun retryReusesIdAndOnlyCompletedUploadsBecomeDuplicates() = runTest {
        val history = MemoryHistory()
        val remote = FlakyUploadRepository()
        val useCase = UploadPhotoUseCase(
            PrepareUploadUseCase(history, Sha256ContentHasher(), UploadIdFactory { "stable-id" }),
            remote,
            history,
        )
        val bytes = "photo bytes".encodeToByteArray()

        assertFailsWith<UploadFailure> { useCase(bytes).toList() }
        assertEquals(0, history.completed)

        val retry = useCase(bytes).toList()
        assertEquals(UploadOutcome.Progress(0.5f), retry.first())
        assertIs<UploadOutcome.Completed>(retry.last())
        assertEquals(1, history.completed)
        assertEquals(listOf("stable-id", "stable-id"), remote.ids)

        assertIs<UploadOutcome.AlreadyUploaded>(useCase(bytes).toList().single())
        assertEquals(2, remote.ids.size)
    }

    private class FlakyUploadRepository : PhotoUploadRepository {
        val ids = mutableListOf<String>()

        override fun upload(clientUploadId: String, bytes: ByteArray): Flow<PhotoUploadEvent> = flow {
            ids += clientUploadId
            emit(PhotoUploadEvent.Progress(0.5f))
            if (ids.size == 1) throw UploadFailure("Connection reset", retryable = true)
            emit(PhotoUploadEvent.Completed(photo()))
        }
    }

    private class MemoryHistory : UploadHistoryRepository {
        private val records = mutableMapOf<String, UploadRecord>()
        var completed = 0

        override suspend fun reserve(fingerprint: String, proposedClientUploadId: String): UploadRecord =
            records.getOrPut(fingerprint) { UploadRecord(fingerprint, proposedClientUploadId, null) }

        override suspend fun complete(fingerprint: String, photo: Photo) {
            records[fingerprint] = requireNotNull(records[fingerprint]).copy(photo = photo)
            completed++
        }

        override fun observeCompleted(): Flow<List<CompletedUpload>> = flowOf(emptyList())
    }

    private companion object {
        fun photo() = Photo(
            id = "uploaded",
            width = 100,
            height = 80,
            createdAt = Instant.fromEpochMilliseconds(1_000),
            thumbnailUrl = "thumbnail",
            previewUrl = "preview",
            imageUrl = "image",
        )
    }
}
