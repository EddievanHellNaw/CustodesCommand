
package com.example.custodescommand.feature.rosters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.custodescommand.core.data.repository.ReferenceRepository
import com.example.custodescommand.core.data.repository.RosterRepository
import com.example.custodescommand.core.domain.model.*
import com.example.custodescommand.core.domain.service.PointsCalculator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class LoadedRoster(
    val roster: Roster?,
    val availableUnits: List<UnitSummary>,
    val points: PointSummary?,
    val pricingError: String?
)

class RosterEditorViewModel(
    private val rosterRepository: RosterRepository,
    private val referenceRepository: ReferenceRepository,
    private val initializeData: suspend () -> Unit
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        RosterEditorUiState()
    )

    val uiState: StateFlow<RosterEditorUiState> =
        _uiState.asStateFlow()

    private var observationJob: Job? = null

    fun loadRoster(rosterId: String) {
        if (
            _uiState.value.rosterId == rosterId &&
            observationJob?.isActive == true
        ) {
            return
        }

        observationJob?.cancel()

        _uiState.value = RosterEditorUiState(
            rosterId = rosterId
        )

        observationJob = viewModelScope.launch {
            try {
                initializeData()

                val catalog =
                    referenceRepository.loadPointsCatalog()

                val calculator = PointsCalculator(catalog)

                combine(
                    rosterRepository.observeRoster(rosterId),
                    referenceRepository.observeUnits()
                ) { roster, units ->

                    val calculation = roster?.let {
                        runCatching {
                            calculator.calculate(it)
                        }
                    }

                    LoadedRoster(
                        roster = roster,
                        availableUnits = units,
                        points = calculation?.getOrNull(),
                        pricingError = calculation
                            ?.exceptionOrNull()
                            ?.message
                    )

                }.collect { loaded ->

                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            roster = loaded.roster,
                            availableUnits = loaded.availableUnits,
                            points = loaded.points,
                            pricingError = loaded.pricingError,
                            loadError = if (
                                loaded.roster == null
                            ) {
                                "Roster not found."
                            } else {
                                null
                            }
                        )
                    }
                }

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadError = exception.message
                            ?: "Unable to load roster."
                    )
                }
            }
        }
    }

    fun addUnit(
        unitId: String,
        modelCount: Int
    ) {
        performEdit { rosterId ->
            rosterRepository.addUnit(
                rosterId = rosterId,
                unitId = unitId,
                modelCount = modelCount
            )
        }
    }

    fun removeUnit(instanceId: String) {
        performEdit { rosterId ->
            rosterRepository.removeUnit(
                rosterId = rosterId,
                instanceId = instanceId
            )
        }
    }

    private fun performEdit(
        action: suspend (String) -> Unit
    ) {
        val rosterId = _uiState.value.rosterId ?: return

        if (_uiState.value.isSaving) return
        if (_uiState.value.roster == null) return

        _uiState.update {
            it.copy(
                isSaving = true,
                actionError = null
            )
        }

        viewModelScope.launch {
            try {
                action(rosterId)

            } catch (exception: CancellationException) {
                throw exception

            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        actionError = exception.message
                            ?: "Unable to update roster."
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
            it.copy(actionError = null)
        }
    }
}
