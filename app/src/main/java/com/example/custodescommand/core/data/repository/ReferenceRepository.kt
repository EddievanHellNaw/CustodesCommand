
package com.example.custodescommand.core.data.repository

import com.example.custodescommand.core.data.local.dao.ReferenceDao
import com.example.custodescommand.core.data.local.entity.UnitEntity
import com.example.custodescommand.core.data.local.entity.PointTierEntity
import com.example.custodescommand.core.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReferenceRepository(
    private val dao: ReferenceDao
) {

    fun observeUnits(): Flow<List<UnitSummary>> {
        return dao.observeUnits().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun findUnit(id: String): UnitSummary? {
        return dao.findUnit(id)?.toDomain()
    }

    suspend fun loadPointsCatalog(): PointsCatalog {
        val metadata = dao.getMetadata()
            ?: error("Points catalogue has not been initialized.")

        return PointsCatalog(
            version = metadata.pointsVersion,
            tiers = dao.getPointTiers().map { it.toDomain() }
        )
    }
}

private fun UnitEntity.toDomain() = UnitSummary(
    id = id,
    name = name,
    category = category,
    allowedModelCounts = allowedModelCounts,
    source = sourceDocument?.let {
        SourceReference(it, sourcePage)
    }
)

private fun PointTierEntity.toDomain() = PointCostTier(
    unitId = unitId,
    modelCount = modelCount,
    fromOccurrence = fromOccurrence,
    throughOccurrence = throughOccurrence,
    points = points
)
