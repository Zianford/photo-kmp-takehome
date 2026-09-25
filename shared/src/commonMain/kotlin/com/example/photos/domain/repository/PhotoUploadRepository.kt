package com.example.photos.domain.repository

import com.example.photos.domain.model.PhotoUploadEvent
import kotlinx.coroutines.flow.Flow

interface PhotoUploadRepository {
    fun upload(clientUploadId: String, bytes: ByteArray): Flow<PhotoUploadEvent>
}
