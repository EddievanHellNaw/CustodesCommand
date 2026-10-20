package com.example.custodescommand.feature.rosters

import com.example.custodescommand.core.domain.model.*

data class RosterEditorUiState(
    val rosterId: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val roster: Roster? = null,
    val availableUnits: List<UnitSummary> = emptyList(),
    // Base-unit costs ONLY, used to label each squad.
    val points: PointSummary? = null,
    val enhancementPoints: Int = 0,
    val assignedEnhancementCount: Int = 0,
    val wargearPoints: Int = 0,
    val configuredWargearCount: Int = 0,
    val loadError: String? = null,
    val actionError: String? = null,
    val pricingError: String? = null
) {
    val armyTotalPoints: Int? get() = points?.totalPoints?.plus(enhancementPoints)?.plus(wargearPoints)
    val armyRemainingPoints: Int? get() = armyTotalPoints?.let { total -> roster?.pointsLimit?.minus(total) }
}
