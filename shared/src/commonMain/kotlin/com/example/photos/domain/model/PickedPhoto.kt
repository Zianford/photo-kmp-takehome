package com.example.photos.domain.model

data class PickedPhoto(val name: String, val bytes: ByteArray)

sealed interface PhotoUploadEvent {
    data class Progress(val fraction: Float) : PhotoUploadEvent
    data class Completed(val photo: Photo) : PhotoUploadEvent
}

class UploadFailure(message: String, val retryable: Boolean) : Exception(message)
