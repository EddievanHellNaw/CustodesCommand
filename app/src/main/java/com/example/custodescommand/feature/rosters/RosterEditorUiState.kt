
package com.example.custodescommand.feature.rosters

import com.example.custodescommand.core.domain.model.*

data class RosterEditorUiState(
    val rosterId: String? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,

    val roster: Roster? = null,
    val availableUnits: List<UnitSummary> = emptyList(),
    val points: PointSummary? = null,

    val loadError: String? = null,
    val actionError: String? = null,
    val pricingError: String? = null
)
