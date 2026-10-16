
package com.example.custodescommand.core.domain.model

data class PointCostTier(
    val unitId: String,
    val modelCount: Int,
    val fromOccurrence: Int,
    val throughOccurrence: Int?,
    val points: Int
) {
    init {
        require(modelCount > 0)
        require(fromOccurrence > 0)
        require(points >= 0)
        require(
            throughOccurrence == null ||
                    throughOccurrence >= fromOccurrence
        )
    }
}

data class PointsCatalog(
    val version: String,
    val tiers: List<PointCostTier>
)

data class PointLine(
    val rosterUnitId: String,
    val unitId: String,
    val modelCount: Int,
    val occurrence: Int,
    val points: Int
)

data class PointSummary(
    val pointsVersion: String,
    val lines: List<PointLine>,
    val totalPoints: Int,
    val remainingPoints: Int
)
