package com.example.custodescommand.core.domain.model

/**
 * Pure Kotlin assembly of a read-only game reference from persisted domain records.
 * No Compose, Room, Android, or mutation of roster data.
 */
object GameModeAssembler {
    fun assemble(
        roster: Roster,
        knownUnits: List<UnitSummary>,
        pointSummary: PointSummary?,
        pricingError: String?,
        selectedDetachmentIds: Set<String>,
        detachmentDetails: List<DetachmentDetail>,
        enhancements: List<AssignedEnhancement>,
        wargear: List<WargearSelection>,
        detailedUnits: Map<String, DetailedUnit>
    ): GameModeSnapshot {
        val summariesById = knownUnits.associateBy { it.id }
        val basePriceByInstance = pointSummary?.lines?.associateBy { it.rosterUnitId }.orEmpty()
        val enhancementsByInstance = enhancements.groupBy { it.rosterUnitInstanceId }
        val wargearByInstance = wargear.groupBy { it.rosterUnitInstanceId }
        val problems = mutableListOf<String>()

        if (pointSummary == null) {
            problems += "Base unit points unavailable: ${pricingError ?: "pricing catalogue missing or incomplete"}."
        }
        if (detachmentDetails.size != selectedDetachmentIds.size) {
            problems += "Some selected detachment definitions could not be loaded. DP used is unknown."
        }
        if (enhancements.any { it.detachmentId !in selectedDetachmentIds }) {
            problems += "An assigned enhancement belongs to a detachment that is not currently selected."
        }

        val rosterUnits = roster.units.map { selection ->
            val detailed = detailedUnits[selection.unitId]
            val equipment = wargearByInstance[selection.instanceId].orEmpty()
            val configured = detailed?.let {
                ConfiguredWeaponResolver.resolve(it, selection.modelCount, equipment)
            }
            GameUnit(
                rosterUnit = selection,
                name = summariesById[selection.unitId]?.name ?: detailed?.name ?: selection.unitId,
                basePoints = basePriceByInstance[selection.instanceId]?.points,
                wargearPoints = equipment.sumOf { it.points },
                enhancements = enhancementsByInstance[selection.instanceId].orEmpty(),
                configuredWeapons = configured,
                sourcePage = detailed?.sourcePage ?: summariesById[selection.unitId]?.source?.page,
                profileStatus = detailed?.profileStatus,
                playStatus = detailed?.playStatus
            )
        }
        val namesByInstance = rosterUnits.associate { it.rosterUnit.instanceId to it.name }
        val sortedDetachments = detachmentDetails.sortedBy { it.summary.name }

        return GameModeSnapshot(
            rosterId = roster.id,
            rosterName = roster.name,
            points = GamePointTotals(
                baseUnitPoints = pointSummary?.totalPoints,
                enhancementPoints = enhancements.sumOf { it.points },
                wargearPoints = wargear.sumOf { it.points },
                pointsLimit = roster.pointsLimit
            ),
            units = rosterUnits,
            detachments = sortedDetachments,
            enhancements = enhancements.map {
                GameEnhancement(it, namesByInstance[it.rosterUnitInstanceId] ?: it.unitId)
            },
            stratagems = sortedDetachments.flatMap { detachment ->
                detachment.stratagems.map { stratagem ->
                    GameStratagem(detachment.summary.id, detachment.summary.name, stratagem)
                }
            }.sortedWith(compareBy({ it.detachmentName }, { it.rule.name })),
            dpUsed = if (detachmentDetails.size == selectedDetachmentIds.size)
                detachmentDetails.sumOf { it.summary.dpCost } else null,
            warnings = (problems +
                "Rules/legality not fully verified: consult original pages for complete stratagem effects, DP restrictions, enhancements and untranscribed wargear.").distinct()
        )
    }
}

/** Convenience filters do not determine actual stratagem eligibility. */
enum class GamePhase(val label: String) {
    ALL("All"), COMMAND("Command"), MOVEMENT("Movement"),
    SHOOTING("Shooting"), CHARGE("Charge"), FIGHT("Fight")
}

object GamePhaseFilter {
    fun matches(whenPhase: String, selected: GamePhase): Boolean {
        if (selected == GamePhase.ALL) return true
        val phase = whenPhase.lowercase()
        if (phase.contains("any phase")) return true
        return phase.contains(selected.label.lowercase())
    }
}
