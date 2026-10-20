package com.example.custodescommand.core.domain.model

/** Read-only projections for a particular saved roster. Not Room entities. */
data class GameUnit(
    val rosterUnit: RosterUnit,
    val name: String,
    val basePoints: Int?,
    val wargearPoints: Int,
    val enhancements: List<AssignedEnhancement>,
    val configuredWeapons: ConfiguredWeaponsResult?,
    val sourcePage: Int?,
    val profileStatus: String?,
    val playStatus: String?
)

data class GameStratagem(
    val detachmentId: String,
    val detachmentName: String,
    val rule: StratagemReference
)

data class GameEnhancement(
    val assignment: AssignedEnhancement,
    val targetUnitName: String
)

data class GamePointTotals(
    val baseUnitPoints: Int?,
    val enhancementPoints: Int,
    val wargearPoints: Int,
    val pointsLimit: Int
) {
    // A missing base-points calculation is UNKNOWN, never silently zero.
    val totalPoints: Int? get() = baseUnitPoints?.let { it + enhancementPoints + wargearPoints }
    val remainingPoints: Int? get() = totalPoints?.let { pointsLimit - it }
}

data class GameModeSnapshot(
    val rosterId: String,
    val rosterName: String,
    val points: GamePointTotals,
    val units: List<GameUnit>,
    val detachments: List<DetachmentDetail>,
    val enhancements: List<GameEnhancement>,
    val stratagems: List<GameStratagem>,
    val dpUsed: Int?,
    val warnings: List<String>
)
