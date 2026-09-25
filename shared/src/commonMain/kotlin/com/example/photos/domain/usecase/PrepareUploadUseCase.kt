package com.example.photos.domain.usecase

import com.example.photos.domain.model.UploadPreparation
import com.example.photos.domain.repository.UploadHistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun interface ContentHasher {
    fun sha256(bytes: ByteArray): String
}

fun interface UploadIdFactory {
    fun create(): String
}

class PrepareUploadUseCase(
    private val history: UploadHistoryRepository,
    private val hasher: ContentHasher,
    private val ids: UploadIdFactory,
) {
    suspend operator fun invoke(bytes: ByteArray): UploadPreparation {
        require(bytes.isNotEmpty()) { "A photo must contain bytes" }
        val fingerprint = withContext(Dispatchers.Default) { hasher.sha256(bytes) }
        val record = history.reserve(fingerprint, ids.create())
        return record.photo?.let(UploadPreparation::AlreadyUploaded)
            ?: UploadPreparation.Ready(record.fingerprint, record.clientUploadId)
    }
}
