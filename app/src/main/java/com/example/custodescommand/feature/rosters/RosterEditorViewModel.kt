package com.example.custodescommand.feature.rosters

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.custodescommand.core.data.repository.ReferenceRepository
import com.example.custodescommand.core.data.repository.RosterRepository
import com.example.custodescommand.core.data.repository.RosterEnhancementRepository
import com.example.custodescommand.core.data.repository.WargearRepository
import com.example.custodescommand.core.domain.model.*
import com.example.custodescommand.core.domain.service.PointsCalculator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.example.custodescommand.core.data.media.UnitPhotoStore

private data class LoadedRoster(
    val roster: Roster?,
    val availableUnits: List<UnitSummary>,
    val points: PointSummary?,
    val enhancementsPoints: Int,
    val assignedCount: Int,
    val wargearPoints: Int,
    val configuredWargearCount: Int,
    val pricingError: String?
)

class RosterEditorViewModel(
    private val rosterRepository: RosterRepository,
    private val referenceRepository: ReferenceRepository,
    private val enhancementRepository: RosterEnhancementRepository,
    private val wargearRepository: WargearRepository,
    private val initializeData: suspend () -> Unit,
    private val unitPhotoStore: UnitPhotoStore
) : ViewModel() {
    private val _uiState = MutableStateFlow(RosterEditorUiState())
    val uiState: StateFlow<RosterEditorUiState> = _uiState.asStateFlow()
    private var observationJob: Job? = null

    fun loadRoster(rosterId: String) {
        if (_uiState.value.rosterId == rosterId && observationJob?.isActive == true) return
        observationJob?.cancel()
        _uiState.value = RosterEditorUiState(rosterId = rosterId)
        observationJob = viewModelScope.launch {
            try {
                initializeData()
                val calculator = PointsCalculator(referenceRepository.loadPointsCatalog())
                combine(
                    rosterRepository.observeRoster(rosterId),
                    referenceRepository.observeUnits(),
                    enhancementRepository.observeAssignments(rosterId),
                    wargearRepository.observeRosterSelections(rosterId)
                ) { roster, units, assigned, wargear ->
                    val calculation = roster?.let { runCatching { calculator.calculate(it) } }
                    LoadedRoster(
                        roster = roster,
                        availableUnits = units,
                        points = calculation?.getOrNull(),
                        enhancementsPoints = assigned.sumOf { it.points },
                        assignedCount = assigned.size,
                        wargearPoints = WargearRules.totalPoints(wargear),
                        configuredWargearCount = wargear.size,
                        pricingError = calculation?.exceptionOrNull()?.message
                    )
                }.collect { loaded ->
                    _uiState.update { current ->
                        current.copy(
                            isLoading = false,
                            roster = loaded.roster,
                            availableUnits = loaded.availableUnits,
                            points = loaded.points,
                            enhancementPoints = loaded.enhancementsPoints,
                            assignedEnhancementCount = loaded.assignedCount,
                            wargearPoints = loaded.wargearPoints,
                            configuredWargearCount = loaded.configuredWargearCount,
                            pricingError = loaded.pricingError,
                            loadError = if (loaded.roster == null) "Roster not found." else null
                        )
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false,
                    loadError = e.message ?: "Unable to load roster.") }
            }
        }
    }

    fun addUnit(unitId: String, modelCount: Int) = performEdit { id ->
        rosterRepository.addUnit(id, unitId, modelCount)
    }

    fun removeUnit(instanceId: String) = performEdit { rosterId ->
        rosterRepository.removeUnit(rosterId, instanceId)
        // If a private photo could not be removed, the database deletion
        // still succeeded; file cleanup is best-effort.
        runCatching { unitPhotoStore.deletePhoto(instanceId) }
    }

    private fun performEdit(action: suspend (String) -> Unit) {
        val id = _uiState.value.rosterId ?: return
        if (_uiState.value.isSaving || _uiState.value.roster == null) return
        _uiState.update { it.copy(isSaving = true, actionError = null) }
        viewModelScope.launch {
            try {
                action(id)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                _uiState.update { it.copy(actionError = e.message ?: "Cannot change roster.") }
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun dismissError() { _uiState.update { it.copy(actionError = null) } }
}
