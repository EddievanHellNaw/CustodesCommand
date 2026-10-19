
package com.example.custodescommand.feature.datasheets

import com.example.custodescommand.core.domain.model.UnitSummary

data class DatasheetsUiState(
    val isLoading: Boolean = true,
    val units: List<UnitSummary> = emptyList(),
    val errorMessage: String? = null
)
