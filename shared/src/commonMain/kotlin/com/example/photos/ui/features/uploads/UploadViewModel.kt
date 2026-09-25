package com.example.photos.ui.features.uploads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photos.domain.model.PickedPhoto
import com.example.photos.domain.model.UploadFailure
import com.example.photos.domain.repository.PhotoPicker
import com.example.photos.domain.usecase.UploadOutcome
import com.example.photos.domain.usecase.UploadPhotoUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

class UploadViewModel(
    private val picker: PhotoPicker,
    private val uploadPhoto: UploadPhotoUseCase,
) : ViewModel() {
    private val mutableState = MutableStateFlow(UploadUiState())
    val state = mutableState.asStateFlow()

    private val bytesById = mutableMapOf<String, ByteArray>()
    private val pending = Channel<String>(Channel.UNLIMITED)

    init {
        viewModelScope.launch {
            for (id in pending) upload(id)
        }
    }

    fun onAction(action: UploadAction) {
        when (action) {
            UploadAction.Pick -> pick()
            is UploadAction.Retry -> retry(action.id)
            UploadAction.DismissError -> mutableState.update { it.copy(pickerError = null) }
        }
    }

    private fun pick() {
        if (state.value.isPicking) return
        mutableState.update { it.copy(isPicking = true, pickerError = null) }
        viewModelScope.launch {
            try {
                picker.pickPhotos().forEach(::enqueue)
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                mutableState.update { it.copy(pickerError = "Couldn't read the selected photos.") }
            } finally {
                mutableState.update { it.copy(isPicking = false) }
            }
        }
    }

    private fun enqueue(photo: PickedPhoto) {
        if (photo.bytes.isEmpty()) return
        if (bytesById.values.any { it.contentEquals(photo.bytes) }) return
        val id = Uuid.random().toString()
        bytesById[id] = photo.bytes
        mutableState.update { it.copy(items = it.items + UploadItemUiState(id, photo.name)) }
        pending.trySend(id)
    }

    private fun retry(id: String) {
        val item = state.value.items.firstOrNull { it.id == id } ?: return
        if (item.status != UploadStatus.Failed || !item.retryable || id !in bytesById) return
        updateItem(id) { it.copy(status = UploadStatus.Queued, error = null, progress = 0f, retryable = false) }
        pending.trySend(id)
    }

    private suspend fun upload(id: String) {
        val bytes = bytesById[id] ?: return
        updateItem(id) { it.copy(status = UploadStatus.Uploading, progress = 0f) }
        try {
            uploadPhoto(bytes).collect { outcome ->
                when (outcome) {
                    is UploadOutcome.Progress -> updateItem(id) { it.copy(progress = outcome.fraction) }
                    is UploadOutcome.AlreadyUploaded -> finish(id, UploadStatus.AlreadyUploaded)
                    is UploadOutcome.Completed -> finish(id, UploadStatus.Completed)
                }
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: Exception) {
            updateItem(id) {
                it.copy(
                    status = UploadStatus.Failed,
                    error = error.message ?: "Upload failed.",
                    retryable = (error as? UploadFailure)?.retryable ?: true,
                )
            }
        }
    }

    private fun finish(id: String, status: UploadStatus) {
        updateItem(id) { it.copy(status = status, progress = 1f) }
        bytesById.remove(id)
    }

    private fun updateItem(id: String, transform: (UploadItemUiState) -> UploadItemUiState) {
        mutableState.update { current ->
            current.copy(items = current.items.map { if (it.id == id) transform(it) else it })
        }
    }
}
