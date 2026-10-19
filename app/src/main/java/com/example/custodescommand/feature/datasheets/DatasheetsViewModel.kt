
package com.example.custodescommand.feature.datasheets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.custodescommand.core.data.repository.ReferenceRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DatasheetsViewModel(
    private val repository: ReferenceRepository,
    private val initializeData: suspend () -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        DatasheetsUiState()
    )

    val uiState: StateFlow<DatasheetsUiState> =
        _uiState.asStateFlow()

    init {
        loadUnits()
    }

    private fun loadUnits() {
        viewModelScope.launch {
            try {
                initializeData()

                repository.observeUnits().collect { units ->
                    _uiState.value = DatasheetsUiState(
                        isLoading = false,
                        units = units
                    )
                }

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                _uiState.value = DatasheetsUiState(
                    isLoading = false,
                    errorMessage = exception.message
                        ?: "Unable to load datasheets."
                )
            }
        }
    }
}
