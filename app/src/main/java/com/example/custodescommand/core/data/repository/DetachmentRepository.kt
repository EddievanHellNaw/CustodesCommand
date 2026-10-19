package com.example.custodescommand.core.data.repository

import androidx.room.withTransaction
import com.example.custodescommand.core.data.local.CustodesDatabase
import com.example.custodescommand.core.data.local.entity.DetachmentEntity
import com.example.custodescommand.core.data.local.entity.RosterDetachmentEntity
import com.example.custodescommand.core.domain.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

class DetachmentRepository(private val db: CustodesDatabase) {
    private val dao = db.detachmentDao()

    fun observeAll(): Flow<List<DetachmentSummary>> =
        dao.observeDetachments().map { rows -> rows.map { it.toSummary() } }

    suspend fun findDetail(id: String): DetachmentDetail? {
        val row = dao.findDetachment(id) ?: return null
        return DetachmentDetail(
            summary = row.toSummary(),
            ruleTitle = row.ruleTitle,
            ruleText = row.ruleText,
            ruleTextStatus = row.ruleTextStatus,
            favouredKatah = row.favouredKatah,
            favouredKatahEffect = row.favouredKatahEffect,
            sourcePages = listOf(row.sourcePage) + row.extraSourcePagesJson.toIntList(),
            stratagems = dao.findStratagems(id).map {
                StratagemReference(
                    id = it.stratagemId, name = it.name, cpCost = it.cpCost,
                    phase = it.phase, whenText = it.whenText,
                    targetText = it.targetText, effectText = it.effectText,
                    textStatus = it.textStatus, sourcePage = it.sourcePage
                )
            },
            enhancements = dao.findEnhancements(id).map {
                EnhancementReference(
                    id = it.enhancementId, name = it.name, points = it.points,
                    effectText = it.effectText, eligibilityText = it.eligibilityText,
                    textStatus = it.textStatus
                )
            }
        )
    }

    fun observeSelectedIds(rosterId: String): Flow<Set<String>> =
        dao.observeRosterSelections(rosterId).map { rows -> rows.map { it.detachmentId }.toSet() }

    suspend fun setSelected(rosterId: String, detachmentId: String, selected: Boolean) {
        db.withTransaction {
            require(db.rosterDao().getRosterWithUnits(rosterId) != null) {
                "Roster not found"
            }
            require(dao.findDetachment(detachmentId) != null) {
                "Detachment not found"
            }
            if (selected) {
                dao.selectForRoster(RosterDetachmentEntity(rosterId, detachmentId))
            } else {
                // The FK also protects integrity; this check provides a useful message.
                require(db.rosterEnhancementDao().countForDetachment(rosterId, detachmentId) == 0) {
                    "Remove enhancements assigned from this detachment before deselecting it."
                }
                dao.deselectForRoster(rosterId, detachmentId)
            }
        }
    }
}

private fun DetachmentEntity.toSummary() = DetachmentSummary(
    id = id, name = name, dpCost = dpCost,
    dispositions = dispositionsJson.toStringList(),
    uniqueShieldHost = uniqueShieldHost
)

private fun String.toStringList(): List<String> = JSONArray(this).let { arr ->
    (0 until arr.length()).map { arr.getString(it) }
}
private fun String.toIntList(): List<Int> = JSONArray(this).let { arr ->
    (0 until arr.length()).map { arr.getInt(it) }
}
