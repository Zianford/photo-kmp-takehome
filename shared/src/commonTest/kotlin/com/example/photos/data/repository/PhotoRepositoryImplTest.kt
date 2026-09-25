package com.example.photos.data.repository

import com.example.photos.api.ApiException
import com.example.photos.api.PhotoApi
import com.example.photos.api.PhotoPage
import com.example.photos.api.UploadEvent
import com.example.photos.domain.model.Photo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class PhotoRepositoryImplTest {
    @Test
    fun previouslyFetchedPhotosRemainAvailableWhenFeedFails() = runTest {
        val snapshots = MemoryPhotoSnapshotStore()
        val api = FakeApi()
        val repository = PhotoRepositoryImpl(api, snapshots)

        assertEquals(listOf("first"), repository.feed().photos.map { it.id })
        assertEquals(listOf("second"), repository.feed(cursor = "next").photos.map { it.id })

        api.offline = true
        val cached = PhotoRepositoryImpl(api, snapshots).feed()
        assertEquals(listOf("first", "second"), cached.photos.map { it.id })
        assertEquals(null, cached.nextCursor)
        assertFailsWith<ApiException> { repository.feed(cursor = "next") }

        api.offline = false
        assertEquals(listOf("first", "second"), repository.feed().photos.map { it.id })
    }

    private class MemoryPhotoSnapshotStore : PhotoSnapshotStore {
        private val items = mutableListOf<Photo>()

        override suspend fun photos(): List<Photo> = items.toList()
        override suspend fun replace(photos: List<Photo>) {
            items.clear()
            items.addAll(photos)
        }
        override suspend fun append(photos: List<Photo>) {
            items.addAll(photos)
        }
    }

    private class FakeApi : PhotoApi {
        var offline = false

        override suspend fun feed(cursor: String?, pageSize: Int): PhotoPage {
            if (offline) throw ApiException("Offline", retryable = true)
            return if (cursor == null) PhotoPage(listOf(photo("first")), "next")
            else PhotoPage(listOf(photo("second")), null)
        }

        override fun upload(clientUploadId: String, bytes: ByteArray): Flow<UploadEvent> =
            error("Unused")

        private fun photo(id: String) = com.example.photos.api.Photo(
            id = id,
            width = 100,
            height = 80,
            createdAt = Instant.fromEpochMilliseconds(1_000),
        )
    }
}
