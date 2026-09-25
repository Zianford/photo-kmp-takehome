package com.example.photos.domain.repository

import com.example.photos.domain.model.CompletedUpload
import com.example.photos.domain.model.Photo
import com.example.photos.domain.model.UploadRecord
import kotlinx.coroutines.flow.Flow

interface UploadHistoryRepository {
    suspend fun reserve(fingerprint: String, proposedClientUploadId: String): UploadRecord
    suspend fun complete(fingerprint: String, photo: Photo)
    fun observeCompleted(): Flow<List<CompletedUpload>>
}
