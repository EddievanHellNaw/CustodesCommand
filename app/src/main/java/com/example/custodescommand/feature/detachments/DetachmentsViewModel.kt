package com.example.custodescommand.feature.detachments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.custodescommand.core.data.repository.DetachmentRepository
import com.example.custodescommand.core.domain.model.DetachmentDetail
import com.example.custodescommand.core.domain.model.DetachmentSummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetachmentsUiState(
    val loading: Boolean = true,
    val detachments: List<DetachmentSummary> = emptyList(),
    val openId: String? = null,
    val detail: DetachmentDetail? = null,
    val loadingDetail: Boolean = false,
    val error: String? = null
)

class DetachmentsViewModel(
    private val repository: DetachmentRepository,
    private val initializeData: suspend () -> Unit
) : ViewModel() {
    private val mutableState = MutableStateFlow(DetachmentsUiState())
    val state: StateFlow<DetachmentsUiState> = mutableState.asStateFlow()
    private var detailJob: Job? = null

    init {
        viewModelScope.launch {
            try {
                initializeData()
                repository.observeAll().collect { records ->
                    mutableState.update { it.copy(loading = false, detachments = records, error = null) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                mutableState.update { it.copy(loading = false, error = e.message ?: "Cannot load detachments") }
            }
        }
    }

    fun open(id: String) {
        if (mutableState.value.openId == id && mutableState.value.detail?.summary?.id == id) return
        detailJob?.cancel()
        mutableState.update { it.copy(openId = id, detail = null, loadingDetail = true) }
        detailJob = viewModelScope.launch {
            try {
                initializeData()
                val details = repository.findDetail(id)
                mutableState.update { it.copy(detail = details, loadingDetail = false,
                    error = if (details == null) "Detachment not found" else null) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                mutableState.update { it.copy(loadingDetail = false, error = e.message ?: "Cannot load details") }
            }
        }
    }
}
