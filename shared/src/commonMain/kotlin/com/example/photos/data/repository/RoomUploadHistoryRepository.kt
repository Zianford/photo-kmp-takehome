package com.example.photos.data.repository

import com.example.photos.data.local.UploadRecordDao
import com.example.photos.data.local.UploadRecordEntity
import com.example.photos.domain.model.CompletedUpload
import com.example.photos.domain.model.Photo
import com.example.photos.domain.model.UploadRecord
import com.example.photos.domain.repository.UploadHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Instant

class RoomUploadHistoryRepository(private val dao: UploadRecordDao) : UploadHistoryRepository {
    override suspend fun reserve(fingerprint: String, proposedClientUploadId: String): UploadRecord {
        val entity = dao.reserve(UploadRecordEntity(fingerprint, proposedClientUploadId))
        return UploadRecord(entity.fingerprint, entity.clientUploadId, entity.toPhoto())
    }

    override suspend fun complete(fingerprint: String, photo: Photo) {
        val changed = dao.complete(
            fingerprint = fingerprint,
            photoId = photo.id,
            photoWidth = photo.width,
            photoHeight = photo.height,
            photoCreatedAtMillis = photo.createdAt.toEpochMilliseconds(),
            thumbnailUrl = photo.thumbnailUrl,
            previewUrl = photo.previewUrl,
            imageUrl = photo.imageUrl,
            completedAtMillis = Clock.System.now().toEpochMilliseconds(),
        )
        check(changed == 1) { "Upload reservation was not found" }
    }

    override fun observeCompleted(): Flow<List<CompletedUpload>> =
        dao.observeCompleted().map { rows ->
            rows.map { row ->
                CompletedUpload(
                    fingerprint = row.fingerprint,
                    clientUploadId = row.clientUploadId,
                    photo = requireNotNull(row.toPhoto()),
                    completedAt = Instant.fromEpochMilliseconds(requireNotNull(row.completedAtMillis)),
                )
            }
        }

    private fun UploadRecordEntity.toPhoto(): Photo? {
        val id = photoId ?: return null
        return Photo(
            id = id,
            width = requireNotNull(photoWidth),
            height = requireNotNull(photoHeight),
            createdAt = Instant.fromEpochMilliseconds(requireNotNull(photoCreatedAtMillis)),
            thumbnailUrl = requireNotNull(thumbnailUrl),
            previewUrl = requireNotNull(previewUrl),
            imageUrl = requireNotNull(imageUrl),
        )
    }
}
