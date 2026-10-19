
package com.example.custodescommand.feature.rosters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.custodescommand.core.data.repository.RosterRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RostersViewModel(
    private val repository: RosterRepository,
    private val initializeData: suspend () -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RostersUiState()
    )

    val uiState: StateFlow<RostersUiState> =
        _uiState.asStateFlow()

    init {
        observeRosters()
    }

    private fun observeRosters() {
        viewModelScope.launch {
            try {
                initializeData()

                repository.observeRosters().collect { rosters ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            rosters = rosters
                        )
                    }
                }

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = exception.message
                            ?: "Unable to load rosters."
                    )
                }
            }
        }
    }

    fun createRoster(
        name: String,
        pointsLimit: Int
    ) {
        if (name.isBlank() || pointsLimit <= 0) {
            _uiState.update {
                it.copy(
                    errorMessage = "Enter a valid roster name and points limit."
                )
            }
            return
        }

        if (_uiState.value.isSaving) return

        _uiState.update {
            it.copy(
                isSaving = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            try {
                initializeData()

                repository.createRoster(
                    name = name,
                    pointsLimit = pointsLimit
                )

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        errorMessage = exception.message
                            ?: "Unable to save roster."
                    )
                }

            } finally {
                _uiState.update {
                    it.copy(isSaving = false)
                }
            }
        }
    }

    fun dismissError() {
        _uiState.update {
            it.copy(errorMessage = null)
        }
    }
}
