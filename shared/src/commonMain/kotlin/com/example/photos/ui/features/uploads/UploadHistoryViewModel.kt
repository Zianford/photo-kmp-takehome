package com.example.photos.ui.features.uploads

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.photos.domain.model.CompletedUpload
import com.example.photos.domain.repository.UploadHistoryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UploadHistoryUiState(
    val uploads: List<CompletedUpload> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
)

class UploadHistoryViewModel(private val history: UploadHistoryRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(UploadHistoryUiState())
    val state = mutableState.asStateFlow()
    private var observeJob: Job? = null

    init {
        observe()
    }

    fun retry() {
        if (observeJob?.isActive == true) return
        observe()
    }

    private fun observe() {
        mutableState.update { it.copy(isLoading = true, error = null) }
        observeJob = viewModelScope.launch {
            try {
                history.observeCompleted().collect { uploads ->
                    mutableState.update { it.copy(uploads = uploads, isLoading = false, error = null) }
                }
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                mutableState.update { it.copy(isLoading = false, error = "Couldn't load upload history.") }
            }
        }
    }
}
