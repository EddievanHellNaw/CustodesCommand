
package com.example.custodescommand.core.domain.model

data class RosterUnit(
    val instanceId: String,
    val unitId: String,
    val modelCount: Int
)

data class Roster(
    val id: String,
    val name: String,
    val pointsLimit: Int,
    val units: List<RosterUnit> = emptyList()
)
