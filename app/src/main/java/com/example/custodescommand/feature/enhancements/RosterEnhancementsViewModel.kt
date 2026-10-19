package com.example.custodescommand.feature.enhancements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.custodescommand.core.data.repository.ReferenceRepository
import com.example.custodescommand.core.data.repository.RosterRepository
import com.example.custodescommand.core.data.repository.RosterEnhancementRepository
import com.example.custodescommand.core.domain.model.*
import com.example.custodescommand.core.domain.service.PointsCalculator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RosterEnhancementsUiState(
    val rosterId: String? = null,
    val loading: Boolean = true,
    val saving: Boolean = false,
    val roster: Roster? = null,
    val unitNames: Map<String, String> = emptyMap(),
    val options: List<AvailableEnhancement> = emptyList(),
    val assignments: List<AssignedEnhancement> = emptyList(),
    val baseUnitPoints: Int? = null,
    val pricingError: String? = null,
    val error: String? = null
) {
    val enhancementPoints: Int get() = assignments.sumOf { it.points }
    val totalPoints: Int? get() = baseUnitPoints?.plus(enhancementPoints)
    val remainingPoints: Int? get() = totalPoints?.let { total -> roster?.pointsLimit?.minus(total) }
    // The lesson-8 enhancement records are INDEX_ONLY; eligibility is not fully verified.
    val provisionalCount: Int get() = assignments.count { it.textStatus != "FULLY_VERIFIED" }
}

class RosterEnhancementsViewModel(
    private val rosterRepository: RosterRepository,
    private val referenceRepository: ReferenceRepository,
    private val enhancements: RosterEnhancementRepository,
    private val initializeData: suspend () -> Unit
) : ViewModel() {
    private val mutableState = MutableStateFlow(RosterEnhancementsUiState())
    val state: StateFlow<RosterEnhancementsUiState> = mutableState.asStateFlow()
    private var observation: Job? = null

    fun load(rosterId: String) {
        if (mutableState.value.rosterId == rosterId && observation?.isActive == true) return
        observation?.cancel()
        mutableState.value = RosterEnhancementsUiState(rosterId = rosterId)
        observation = viewModelScope.launch {
            try {
                initializeData()
                val calculator = PointsCalculator(referenceRepository.loadPointsCatalog())
                combine(
                    rosterRepository.observeRoster(rosterId),
                    referenceRepository.observeUnits(),
                    enhancements.observeAvailable(rosterId),
                    enhancements.observeAssignments(rosterId)
                ) { roster, unitSummaries, options, assigned ->
                    val attempt = roster?.let { runCatching { calculator.calculate(it) } }
                    RosterEnhancementsUiState(
                        rosterId = rosterId,
                        loading = false,
                        roster = roster,
                        unitNames = unitSummaries.associate { it.id to it.name },
                        options = options,
                        assignments = assigned,
                        baseUnitPoints = attempt?.getOrNull()?.totalPoints,
                        pricingError = attempt?.exceptionOrNull()?.message,
                        error = if (roster == null) "Roster not found" else null
                    )
                }.collect { incoming ->
                    mutableState.update { current ->
                        incoming.copy(saving = current.saving, error = current.error ?: incoming.error)
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                mutableState.update { it.copy(loading = false, error = e.message ?: "Cannot open enhancements") }
            }
        }
    }

    fun assign(enhancement: AvailableEnhancement, rosterUnitInstanceId: String) {
        val id = mutableState.value.rosterId ?: return
        mutate {
            enhancements.assign(id, enhancement.detachmentId, enhancement.enhancementId, rosterUnitInstanceId)
        }
    }

    fun remove(assignment: AssignedEnhancement) {
        val id = mutableState.value.rosterId ?: return
        mutate { enhancements.unassign(id, assignment.detachmentId, assignment.enhancementId) }
    }

    fun dismissError() {
        mutableState.update { it.copy(error = null) }
    }

    private fun mutate(action: suspend () -> Unit) {
        if (mutableState.value.loading || mutableState.value.saving) return
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                mutableState.update { it.copy(error = e.message ?: "Unable to save enhancement") }
            } finally {
                mutableState.update { it.copy(saving = false) }
            }
        }
    }
}
