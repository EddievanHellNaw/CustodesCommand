
package com.example.custodescommand.feature.rosters

import com.example.custodescommand.core.domain.model.RosterSummary

data class RostersUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val rosters: List<RosterSummary> = emptyList(),
    val errorMessage: String? = null
)
