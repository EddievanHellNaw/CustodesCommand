
package com.example.custodescommand.core.domain.service

import com.example.custodescommand.core.domain.model.*

class PointsCalculator(
    private val catalog: PointsCatalog
) {

    fun calculate(roster: Roster): PointSummary {

        val occurrences = mutableMapOf<String, Int>()

        val lines = roster.units.map { rosterUnit ->

            require(rosterUnit.modelCount > 0) {
                "Model count must be positive."
            }

            val occurrence =
                (occurrences[rosterUnit.unitId] ?: 0) + 1

            occurrences[rosterUnit.unitId] = occurrence

            val matchingTiers = catalog.tiers.filter { tier ->
                tier.unitId == rosterUnit.unitId &&
                        tier.modelCount == rosterUnit.modelCount &&
                        occurrence >= tier.fromOccurrence &&
                        (
                                tier.throughOccurrence == null ||
                                        occurrence <= tier.throughOccurrence
                                )
            }

            require(matchingTiers.size == 1) {
                "Expected exactly one points tier for " +
                        "${rosterUnit.unitId}, " +
                        "${rosterUnit.modelCount} models, " +
                        "occurrence $occurrence. " +
                        "Found ${matchingTiers.size}."
            }

            val tier = matchingTiers.single()

            PointLine(
                rosterUnitId = rosterUnit.instanceId,
                unitId = rosterUnit.unitId,
                modelCount = rosterUnit.modelCount,
                occurrence = occurrence,
                points = tier.points
            )
        }

        val total = lines.sumOf { it.points }

        return PointSummary(
            pointsVersion = catalog.version,
            lines = lines,
            totalPoints = total,
            remainingPoints = roster.pointsLimit - total
        )
    }
}
