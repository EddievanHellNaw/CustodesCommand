
package com.example.custodescommand.core.data.repository

import androidx.room.withTransaction
import com.example.custodescommand.core.data.local.CustodesDatabase
import com.example.custodescommand.core.data.local.entity.*
import com.example.custodescommand.core.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

class RosterRepository(
    private val database: CustodesDatabase
) {
    private val dao = database.rosterDao()
    private val referenceDao = database.referenceDao()

    fun observeRosters(): Flow<List<RosterSummary>> {
        return dao.observeRosters().map { rosters ->
            rosters.map {
                RosterSummary(
                    id = it.id,
                    name = it.name,
                    pointsLimit = it.pointsLimit
                )
            }
        }
    }

    suspend fun createRoster(
        name: String,
        pointsLimit: Int
    ): String {
        require(name.isNotBlank())
        require(pointsLimit > 0)

        val id = UUID.randomUUID().toString()

        dao.insertRoster(
            RosterEntity(
                id = id,
                name = name.trim(),
                pointsLimit = pointsLimit
            )
        )

        return id
    }

    suspend fun addUnit(
        rosterId: String,
        unitId: String,
        modelCount: Int
    ) {
        database.withTransaction {

            val unit = referenceDao.findUnit(unitId)
                ?: error("Unit not found: $unitId")

            require(modelCount in unit.allowedModelCounts) {
                "Unsupported model count for $unitId"
            }

            require(dao.getRosterWithUnits(rosterId) != null) {
                "Roster not found: $rosterId"
            }

            val position = dao.nextPosition(rosterId)

            dao.insertRosterUnit(
                RosterUnitEntity(
                    instanceId = UUID.randomUUID().toString(),
                    rosterId = rosterId,
                    unitId = unitId,
                    modelCount = modelCount,
                    position = position
                )
            )
        }
    }

    // NEW: Remove one squad, not every squad of this type.
    suspend fun removeUnit(
        rosterId: String,
        instanceId: String
    ) {
        val deletedRows = dao.deleteRosterUnit(
            rosterId = rosterId,
            instanceId = instanceId
        )

        require(deletedRows == 1) {
            "Roster unit not found: $instanceId"
        }
    }

    // NEW: Observe one roster and all its squads.
    fun observeRoster(id: String): Flow<Roster?> {
        return dao.observeRosterWithUnits(id).map { result ->
            result?.toDomain()
        }
    }

    suspend fun getRoster(id: String): Roster? {
        return dao.getRosterWithUnits(id)?.toDomain()
    }

    suspend fun deleteRoster(id: String) {
        dao.deleteRoster(id)
    }
}

// Convert Room records into our domain model.
private fun RosterWithUnits.toDomain(): Roster {
    return Roster(
        id = roster.id,
        name = roster.name,
        pointsLimit = roster.pointsLimit,
        units = units
            .sortedBy { it.position }
            .map { unit ->
                RosterUnit(
                    instanceId = unit.instanceId,
                    unitId = unit.unitId,
                    modelCount = unit.modelCount
                )
            }
    )
}
