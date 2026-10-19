package com.example.custodescommand.feature.detachments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.custodescommand.core.data.repository.DetachmentRepository
import com.example.custodescommand.core.domain.model.DetachmentSummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RosterDetachmentsUiState(
    val rosterId: String? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val detachments: List<DetachmentSummary> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val error: String? = null
) {
    val usedDp: Int get() = detachments.filter { it.id in selectedIds }.sumOf { it.dpCost }
}

class RosterDetachmentsViewModel(
    private val repository: DetachmentRepository,
    private val initializeData: suspend () -> Unit
) : ViewModel() {
    private val mutableState = MutableStateFlow(RosterDetachmentsUiState())
    val state: StateFlow<RosterDetachmentsUiState> = mutableState.asStateFlow()
    private var watchJob: Job? = null

    fun loadRoster(rosterId: String) {
        if (mutableState.value.rosterId == rosterId && watchJob?.isActive == true) return
        watchJob?.cancel()
        mutableState.value = RosterDetachmentsUiState(rosterId = rosterId)
        watchJob = viewModelScope.launch {
            try {
                initializeData()
                combine(
                    repository.observeAll(),
                    repository.observeSelectedIds(rosterId)
                ) { all, chosen -> all to chosen }.collect { (all, chosen) ->
                    mutableState.update { it.copy(loading = false, detachments = all, selectedIds = chosen) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                mutableState.update { it.copy(loading = false, error = e.message ?: "Cannot load selections") }
            }
        }
    }

    fun setSelected(detachmentId: String, selected: Boolean) {
        val rosterId = mutableState.value.rosterId ?: return
        if (mutableState.value.saving || mutableState.value.loading) return
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                repository.setSelected(rosterId, detachmentId, selected)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                mutableState.update { it.copy(error = e.message ?: "Unable to change detachment") }
            } finally {
                mutableState.update { it.copy(saving = false) }
            }
        }
    }
}
