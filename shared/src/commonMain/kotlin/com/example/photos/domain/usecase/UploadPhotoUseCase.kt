package com.example.photos.domain.usecase

import com.example.photos.domain.model.PhotoUploadEvent
import com.example.photos.domain.model.UploadPreparation
import com.example.photos.domain.repository.PhotoUploadRepository
import com.example.photos.domain.repository.UploadHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed interface UploadOutcome {
    data class Progress(val fraction: Float) : UploadOutcome
    data class AlreadyUploaded(val photoId: String) : UploadOutcome
    data class Completed(val photoId: String) : UploadOutcome
}

class UploadPhotoUseCase(
    private val prepare: PrepareUploadUseCase,
    private val uploads: PhotoUploadRepository,
    private val history: UploadHistoryRepository,
) {
    operator fun invoke(bytes: ByteArray): Flow<UploadOutcome> = flow {
        when (val prepared = prepare(bytes)) {
            is UploadPreparation.AlreadyUploaded -> emit(UploadOutcome.AlreadyUploaded(prepared.photo.id))
            is UploadPreparation.Ready -> uploads.upload(prepared.clientUploadId, bytes).collect { event ->
                when (event) {
                    is PhotoUploadEvent.Progress -> emit(UploadOutcome.Progress(event.fraction))
                    is PhotoUploadEvent.Completed -> {
                        history.complete(prepared.fingerprint, event.photo)
                        emit(UploadOutcome.Completed(event.photo.id))
                    }
                }
            }
        }
    }
}
