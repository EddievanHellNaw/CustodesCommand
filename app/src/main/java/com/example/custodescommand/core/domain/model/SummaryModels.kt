
package com.example.custodescommand.core.domain.model

data class UnitSummary(
    val id: String,
    val name: String,
    val category: String,
    val allowedModelCounts: Set<Int>,
    val source: SourceReference?
)

data class RosterSummary(
    val id: String,
    val name: String,
    val pointsLimit: Int
)
