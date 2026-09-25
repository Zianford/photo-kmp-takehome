package com.example.photos.domain.usecase

import com.example.photos.data.repository.Sha256ContentHasher
import com.example.photos.domain.model.CompletedUpload
import com.example.photos.domain.model.Photo
import com.example.photos.domain.model.UploadPreparation
import com.example.photos.domain.model.UploadRecord
import com.example.photos.domain.repository.UploadHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals
import kotlin.time.Instant

class PrepareUploadUseCaseTest {
    @Test
    fun identicalBytesReuseIdAndCompletedUploadIsSkipped() = runTest {
        val history = MemoryHistory()
        var nextId = 0
        val prepare = PrepareUploadUseCase(history, Sha256ContentHasher(), UploadIdFactory { "id-${++nextId}" })
        val bytes = "a photo".encodeToByteArray()

        val first = assertIs<UploadPreparation.Ready>(prepare(bytes))
        val retry = assertIs<UploadPreparation.Ready>(prepare(bytes.copyOf()))
        assertEquals(first.fingerprint, retry.fingerprint)
        assertEquals(first.clientUploadId, retry.clientUploadId)

        history.complete(first.fingerprint, photo())
        val duplicate = assertIs<UploadPreparation.AlreadyUploaded>(prepare(bytes))
        assertEquals("uploaded", duplicate.photo.id)

        val different = assertIs<UploadPreparation.Ready>(prepare("another photo".encodeToByteArray()))
        assertNotEquals(first.fingerprint, different.fingerprint)
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

    private fun photo() = Photo(
        id = "uploaded",
        width = 100,
        height = 80,
        createdAt = Instant.fromEpochMilliseconds(1_000),
        thumbnailUrl = "thumbnail",
        previewUrl = "preview",
        imageUrl = "image",
    )
}
