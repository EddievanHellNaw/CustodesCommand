package com.example.custodescommand.feature.datasheets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.custodescommand.core.data.repository.UnitDetailRepository
import com.example.custodescommand.core.domain.model.DetailedUnit
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Kept small and explicit for the existing Lesson 5 Activity-scoped factory pattern.
data class UnitDetailUiState(
    val requestedUnitId: String? = null,
    val loading: Boolean = true,
    val unit: DetailedUnit? = null,
    val error: String? = null
)

class UnitDetailViewModel(
    private val repository: UnitDetailRepository,
    private val initializeData: suspend () -> Unit
) : ViewModel() {
    private val mutableState = MutableStateFlow(UnitDetailUiState())
    val state: StateFlow<UnitDetailUiState> = mutableState.asStateFlow()
    private var job: Job? = null

    fun open(unitId: String) {
        if (state.value.requestedUnitId == unitId && job?.isActive == true) return
        job?.cancel()
        mutableState.value = UnitDetailUiState(requestedUnitId = unitId)
        job = viewModelScope.launch {
            try {
                initializeData()
                val result = repository.findUnit(unitId)
                mutableState.value = UnitDetailUiState(
                    requestedUnitId = unitId,
                    loading = false,
                    unit = result,
                    error = if (result == null) "Unit not found" else null
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                mutableState.value = UnitDetailUiState(
                    requestedUnitId = unitId, loading = false,
                    error = e.message ?: "Could not load datasheet"
                )
            }
        }
    }
}
