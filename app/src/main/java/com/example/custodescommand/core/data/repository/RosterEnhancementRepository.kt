package com.example.custodescommand.core.data.repository

import androidx.room.withTransaction
import com.example.custodescommand.core.data.local.CustodesDatabase
import com.example.custodescommand.core.data.local.entity.RosterEnhancementEntity
import com.example.custodescommand.core.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RosterEnhancementRepository(private val db: CustodesDatabase) {
    private val dao = db.rosterEnhancementDao()

    fun observeAvailable(rosterId: String): Flow<List<AvailableEnhancement>> =
        dao.observeAvailable(rosterId).map { rows ->
            rows.map { e ->
                AvailableEnhancement(
                    e.detachmentId, e.enhancementId, e.name,
                    e.points, e.textStatus, e.eligibilityText
                )
            }
        }

    fun observeAssignments(rosterId: String): Flow<List<AssignedEnhancement>> =
        dao.observeAssignments(rosterId).map { rows ->
            rows.map { a ->
                AssignedEnhancement(
                    a.detachmentId, a.enhancementId, a.rosterUnitInstanceId,
                    a.unitId, a.name, a.points, a.textStatus,
                    a.eligibilityText, a.sourcePage
                )
            }
        }

    suspend fun assign(
        rosterId: String,
        detachmentId: String,
        enhancementId: String,
        rosterUnitInstanceId: String
    ) = db.withTransaction {
        val roster = requireNotNull(db.rosterDao().getRosterWithUnits(rosterId)) {
            "Roster not found"
        }
        val unit = roster.units.singleOrNull { it.instanceId == rosterUnitInstanceId }
            ?: throw IllegalArgumentException("Unit does not belong to this roster")
        require(dao.countSelectedDetachment(rosterId, detachmentId) == 1) {
            "Select this detachment before assigning an enhancement."
        }
        val enhancement = requireNotNull(dao.findEnhancement(detachmentId, enhancementId)) {
            "Enhancement does not exist in this detachment."
        }
        require(dao.countForUnit(rosterUnitInstanceId) == 0) {
            "Only one enhancement can be assigned to this roster unit in this editor version."
        }
        val eligibility = EnhancementEligibility.check(enhancement.enhancementId, unit.unitId)
        require(eligibility.hardErrors.isEmpty()) {
            eligibility.hardErrors.joinToString(" ")
        }
        // Primary key rejects assigning the same enhancement twice to this roster.
        dao.insert(RosterEnhancementEntity(
            rosterId, detachmentId, enhancementId, rosterUnitInstanceId
        ))
    }

    suspend fun unassign(
        rosterId: String, detachmentId: String, enhancementId: String
    ) {
        require(dao.delete(rosterId, detachmentId, enhancementId) == 1) {
            "Enhancement assignment no longer exists."
        }
    }

    suspend fun countAssignedInDetachment(rosterId: String, detachmentId: String): Int =
        dao.countForDetachment(rosterId, detachmentId)
}
