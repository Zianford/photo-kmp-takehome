package com.example.photos.data.repository

import com.example.photos.api.PhotoApi
import com.example.photos.api.ApiException
import com.example.photos.api.UploadEvent
import com.example.photos.data.mapper.toDomain
import com.example.photos.domain.model.PhotoUploadEvent
import com.example.photos.domain.model.UploadFailure
import com.example.photos.domain.repository.PhotoUploadRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class PhotoUploadRepositoryImpl(private val api: PhotoApi) : PhotoUploadRepository {
    override fun upload(clientUploadId: String, bytes: ByteArray): Flow<PhotoUploadEvent> =
        api.upload(clientUploadId, bytes).map { event ->
            when (event) {
                is UploadEvent.Progress -> PhotoUploadEvent.Progress(event.fraction)
                is UploadEvent.Completed -> PhotoUploadEvent.Completed(event.photo.toDomain())
            }
        }.catch { error ->
            if (error is ApiException) throw UploadFailure(error.message ?: "Upload failed", error.retryable)
            throw error
        }
}
