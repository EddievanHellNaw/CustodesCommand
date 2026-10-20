package com.example.custodescommand.feature.gamemode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.custodescommand.core.data.repository.DetachmentRepository
import com.example.custodescommand.core.data.repository.ReferenceRepository
import com.example.custodescommand.core.data.repository.RosterEnhancementRepository
import com.example.custodescommand.core.data.repository.RosterRepository
import com.example.custodescommand.core.data.repository.UnitDetailRepository
import com.example.custodescommand.core.data.repository.WargearRepository
import com.example.custodescommand.core.domain.model.*
import com.example.custodescommand.core.domain.service.PointsCalculator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** The sole state published to the read-only Game Mode screen. */
data class GameModeUiState(
    val rosterId: String? = null,
    val loading: Boolean = true,
    val game: GameModeSnapshot? = null,
    val error: String? = null
)

private data class GameInputs(
    val roster: Roster?,
    val selectedIds: Set<String>,
    val assignments: List<AssignedEnhancement>,
    val equipment: List<WargearSelection>,
    val unitSummaries: List<UnitSummary>
)

class GameModeViewModel(
    private val rosters: RosterRepository,
    private val references: ReferenceRepository,
    private val details: UnitDetailRepository,
    private val detachments: DetachmentRepository,
    private val enhancements: RosterEnhancementRepository,
    private val wargear: WargearRepository,
    private val initializeData: suspend () -> Unit
) : ViewModel() {
    private val mutableState = MutableStateFlow(GameModeUiState())
    val state: StateFlow<GameModeUiState> = mutableState.asStateFlow()
    private var watcher: Job? = null

    fun open(rosterId: String) {
        if (mutableState.value.rosterId == rosterId && watcher?.isActive == true) return
        watcher?.cancel()
        mutableState.value = GameModeUiState(rosterId = rosterId)

        watcher = viewModelScope.launch {
            try {
                initializeData()
                val calculator = PointsCalculator(references.loadPointsCatalog())
                combine(
                    rosters.observeRoster(rosterId),
                    detachments.observeSelectedIds(rosterId),
                    enhancements.observeAssignments(rosterId),
                    wargear.observeRosterSelections(rosterId),
                    references.observeUnits()
                ) { roster, selected, assigned, equipped, summaries ->
                    GameInputs(roster, selected, assigned, equipped, summaries)
                }.collectLatest { input ->
                    val roster = input.roster
                    if (roster == null) {
                        mutableState.value = GameModeUiState(
                            rosterId = rosterId, loading = false,
                            error = "This saved roster could not be found."
                        )
                        return@collectLatest
                    }

                    // Load each distinct datasheet once, even if it appears in multiple squads.
                    val detailedUnits = roster.units.map { it.unitId }.distinct()
                        .mapNotNull { id -> details.findUnit(id)?.let { id to it } }
                        .toMap()
                    val detachmentDetails = input.selectedIds.sorted()
                        .mapNotNull { detachments.findDetail(it) }
                    val pointResult = runCatching { calculator.calculate(roster) }
                    val snapshot = GameModeAssembler.assemble(
                        roster = roster,
                        knownUnits = input.unitSummaries,
                        pointSummary = pointResult.getOrNull(),
                        pricingError = pointResult.exceptionOrNull()?.message,
                        selectedDetachmentIds = input.selectedIds,
                        detachmentDetails = detachmentDetails,
                        enhancements = input.assignments,
                        wargear = input.equipment,
                        detailedUnits = detailedUnits
                    )
                    mutableState.value = GameModeUiState(
                        rosterId = rosterId, loading = false, game = snapshot
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                mutableState.value = GameModeUiState(
                    rosterId = rosterId,
                    loading = false,
                    error = exception.message ?: "Game Mode could not load this roster."
                )
            }
        }
    }
}
