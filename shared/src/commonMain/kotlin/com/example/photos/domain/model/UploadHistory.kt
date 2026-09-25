package com.example.photos.domain.model

import kotlin.time.Instant

data class UploadRecord(
    val fingerprint: String,
    val clientUploadId: String,
    val photo: Photo?,
)

data class CompletedUpload(
    val fingerprint: String,
    val clientUploadId: String,
    val photo: Photo,
    val completedAt: Instant,
)

sealed interface UploadPreparation {
    data class Ready(val fingerprint: String, val clientUploadId: String) : UploadPreparation
    data class AlreadyUploaded(val photo: Photo) : UploadPreparation
}
