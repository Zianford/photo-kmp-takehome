package com.example.photos.ui.features.uploads

data class UploadUiState(
    val items: List<UploadItemUiState> = emptyList(),
    val isPicking: Boolean = false,
    val pickerError: String? = null,
)

data class UploadItemUiState(
    val id: String,
    val name: String,
    val progress: Float = 0f,
    val status: UploadStatus = UploadStatus.Queued,
    val error: String? = null,
    val retryable: Boolean = false,
)

enum class UploadStatus { Queued, Uploading, Completed, AlreadyUploaded, Failed }

sealed interface UploadAction {
    data object Pick : UploadAction
    data class Retry(val id: String) : UploadAction
    data object DismissError : UploadAction
}
