package com.example.custodescommand.feature.wargear

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.custodescommand.core.data.repository.ReferenceRepository
import com.example.custodescommand.core.data.repository.RosterRepository
import com.example.custodescommand.core.data.repository.WargearRepository
import com.example.custodescommand.core.data.repository.UnitDetailRepository
import com.example.custodescommand.core.domain.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WargearEditorUiState(
    val rosterId: String? = null,
    val instanceId: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val unit: RosterUnit? = null,
    val unitName: String = "",
    val allowedSizes: List<Int> = emptyList(),
    val sourcePage: Int? = null,
    val options: List<WargearOption> = emptyList(),
    val selections: List<WargearSelection> = emptyList(),
    val configuredWeapons: ConfiguredWeaponsResult? = null,
    val error: String? = null
) {
    val extraPoints: Int get() = WargearRules.totalPoints(selections)
    val provisionalCount: Int get() = selections.count { it.sourceStatus == "PRICE_ONLY" }
}

class WargearEditorViewModel(
    private val rosters: RosterRepository,
    private val references: ReferenceRepository,
    private val wargear: WargearRepository,
    private val details: UnitDetailRepository,
    private val initializeData: suspend () -> Unit
) : ViewModel() {
    private val mutableState = MutableStateFlow(WargearEditorUiState())
    val state: StateFlow<WargearEditorUiState> = mutableState.asStateFlow()
    private var observation: Job? = null

    fun load(rosterId: String, instanceId: String) {
        if (mutableState.value.rosterId == rosterId &&
            mutableState.value.instanceId == instanceId && observation?.isActive == true) return
        observation?.cancel()
        mutableState.value = WargearEditorUiState(rosterId = rosterId, instanceId = instanceId)
        observation = viewModelScope.launch {
            try {
                initializeData()
                val unitId = requireNotNull(rosters.getRoster(rosterId)
                    ?.units?.firstOrNull { it.instanceId == instanceId }) {
                        "This roster unit could not be found."
                    }.unitId
                val unitSummary = requireNotNull(references.findUnit(unitId)) {
                    "Unit definition was not found."
                }
                val detailedUnit = details.findUnit(unitId)
                combine(
                    rosters.observeRoster(rosterId),
                    wargear.observeOptions(unitId),
                    wargear.observeInstanceSelections(instanceId)
                ) { roster, options, selections ->
                    val target = roster?.units?.firstOrNull { it.instanceId == instanceId }
                    WargearEditorUiState(
                        rosterId = rosterId,
                        instanceId = instanceId,
                        isLoading = false,
                        unit = target,
                        unitName = unitSummary.name,
                        allowedSizes = unitSummary.allowedModelCounts.sorted(),
                        sourcePage = unitSummary.source?.page,
                        options = options,
                        selections = selections,
                        configuredWeapons = if (target != null && detailedUnit != null) {
                            ConfiguredWeaponResolver.resolve(detailedUnit, target.modelCount, selections)
                        } else null
                    )
                }.collect { incoming ->
                    mutableState.update { old -> incoming.copy(
                        isSaving = old.isSaving,
                        error = old.error
                    ) }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                mutableState.update { it.copy(isLoading = false, error = e.message ?: "Could not load equipment.") }
            }
        }
    }

    fun setSelected(optionId: String, modelIndex: Int, selected: Boolean) {
        val current = mutableState.value
        val rosterId = current.rosterId ?: return
        val instanceId = current.instanceId ?: return
        mutate { wargear.select(rosterId, instanceId, optionId, modelIndex, selected) }
    }

    /** For a once-per-unit Vexilla: choose a single model bearer or None. */
    fun setVexillaBearer(bearer: Int?) {
        val current = mutableState.value
        val rosterId = current.rosterId ?: return
        val instanceId = current.instanceId ?: return
        mutate { wargear.setVexillaBearer(rosterId, instanceId, bearer) }
    }

    fun resize(newModelCount: Int) {
        val current = mutableState.value
        val rosterId = current.rosterId ?: return
        val instanceId = current.instanceId ?: return
        mutate { wargear.changeModelCount(rosterId, instanceId, newModelCount) }
    }

    fun dismissError() { mutableState.update { it.copy(error = null) } }

    private fun mutate(action: suspend () -> Unit) {
        if (mutableState.value.isLoading || mutableState.value.isSaving || mutableState.value.unit == null) return
        mutableState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            try {
                action()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Exception) {
                mutableState.update { it.copy(error = e.message ?: "Equipment could not be saved.") }
            } finally {
                mutableState.update { it.copy(isSaving = false) }
            }
        }
    }
}
