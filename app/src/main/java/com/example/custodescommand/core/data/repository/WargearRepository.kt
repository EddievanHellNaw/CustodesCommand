package com.example.custodescommand.core.data.repository

import androidx.room.withTransaction
import com.example.custodescommand.core.data.local.CustodesDatabase
import com.example.custodescommand.core.data.local.entity.*
import com.example.custodescommand.core.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Owns all editable equipment invariants; UI simply sends commands. */
class WargearRepository(private val db: CustodesDatabase) {
    private val dao = db.wargearDao()

    fun observeOptions(unitId: String): Flow<List<WargearOption>> =
        dao.observeOptions(unitId).map { list -> list.map { it.toDomain() } }

    fun observeInstanceSelections(instanceId: String): Flow<List<WargearSelection>> =
        dao.observeInstanceSelections(instanceId).map { list -> list.map { it.toDomain() } }

    fun observeRosterSelections(rosterId: String): Flow<List<WargearSelection>> =
        dao.observeRosterSelections(rosterId).map { list -> list.map { it.toDomain() } }

    suspend fun select(
        rosterId: String,
        instanceId: String,
        optionId: String,
        modelIndex: Int,
        selected: Boolean
    ): WargearCheck = db.withTransaction {
        val unit = requireRosterUnit(rosterId, instanceId)
        val option = requireNotNull(dao.findOption(unit.unitId, optionId)) {
            "This equipment is not a selectable option for this datasheet."
        }
        if (!selected) {
            require(dao.deleteSelection(instanceId, optionId, modelIndex) == 1) {
                "Equipment is not currently selected for this model."
            }
            return@withTransaction WargearCheck()
        }
        val definitions = dao.getOptions(unit.unitId).map { it.toDomain() }
        val existing = dao.getInstanceSelections(instanceId).map { it.toDomain() }
        val check = WargearRules.checkAdd(
            option = option.toDomain(), unitId = unit.unitId,
            modelCount = unit.modelCount, modelIndex = modelIndex,
            alreadySelected = existing, definitions = definitions
        )
        require(check.permitted) { check.errors.joinToString(" ") }
        dao.insertSelection(RosterWargearSelectionEntity(
            rosterUnitInstanceId = instanceId,
            unitId = unit.unitId,
            optionId = optionId,
            modelIndex = modelIndex
        ))
        check
    }

    /**
     * Set the ONE bearer of a once-per-unit Vexilla. Null means remove it.
     * The clear and insert are one database transaction: no intermediate state is committed.
     * Other squads can have their own Vexilla because we scope by rosterUnitInstanceId.
     */
    suspend fun setVexillaBearer(
        rosterId: String,
        instanceId: String,
        bearerModelIndex: Int?
    ) = db.withTransaction {
        val squad = requireRosterUnit(rosterId, instanceId)
        val option = requireNotNull(dao.findOption(squad.unitId, "vexilla")) {
            "A Vexilla is not a selectable option for this datasheet."
        }
        require(option.maxPerUnit == 1) {
            "The Vexilla definition must specify maximum one per unit."
        }
        if (bearerModelIndex != null) require(bearerModelIndex in 1..squad.modelCount) {
            "The bearer must be a model in this squad."
        }
        val existing = dao.getInstanceSelections(instanceId).map { it.toDomain() }
        // Exclude only this unit's existing Vexilla. All other selections remain.
        val otherSelections = existing.filterNot { it.optionId == "vexilla" }
        if (bearerModelIndex != null) {
            val result = WargearRules.checkAdd(
                option = option.toDomain(),
                unitId = squad.unitId,
                modelCount = squad.modelCount,
                modelIndex = bearerModelIndex,
                alreadySelected = otherSelections,
                definitions = dao.getOptions(squad.unitId).map { it.toDomain() }
            )
            require(result.permitted) { result.errors.joinToString(" ") }
        }
        dao.clearOptionSelections(instanceId, "vexilla")
        if (bearerModelIndex != null) dao.insertSelection(
            RosterWargearSelectionEntity(
                rosterUnitInstanceId = instanceId,
                unitId = squad.unitId,
                optionId = "vexilla",
                modelIndex = bearerModelIndex
            )
        )
    }

    /** Never silently discard a model's equipment when its squad is reduced. */
    suspend fun changeModelCount(rosterId: String, instanceId: String, newCount: Int) {
        db.withTransaction {
            val unit = requireRosterUnit(rosterId, instanceId)
            val definition = requireNotNull(db.referenceDao().findUnit(unit.unitId)) {
                "Referenced datasheet no longer exists."
            }
            require(newCount in definition.allowedModelCounts) {
                "This datasheet does not allow $newCount model(s)."
            }
            val selections = dao.getInstanceSelections(instanceId).map { it.toDomain() }
            val result = WargearRules.validateResize(newCount, selections)
            require(result.permitted) { result.errors.joinToString(" ") }
            require(db.rosterDao().changeModelCount(rosterId, instanceId, newCount) == 1) {
                "Could not update this squad."
            }
        }
    }

    private suspend fun requireRosterUnit(rosterId: String, instanceId: String): RosterUnitEntity {
        val roster = requireNotNull(db.rosterDao().getRosterWithUnits(rosterId)) { "Roster not found." }
        return roster.units.singleOrNull { it.instanceId == instanceId }
            ?: throw IllegalArgumentException("Squad is not part of this roster.")
    }
}

private fun WargearDefinitionEntity.toDomain() = WargearOption(
    unitId, optionId, name, pointsPerSelection, maxPerUnit, exclusivityGroup,
    sourceStatus, sourcePage, description
)
private fun WargearSelectionRow.toDomain() = WargearSelection(
    rosterUnitInstanceId, unitId, optionId, modelIndex, name, points, sourceStatus
)
