package com.example.custodescommand.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.custodescommand.core.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RosterEnhancementDao {
    @Query("""
        SELECT a.rosterId, a.detachmentId, a.enhancementId,
               a.rosterUnitInstanceId, u.unitId,
               e.name, e.points, e.textStatus, e.eligibilityText,
               d.sourcePage
        FROM roster_enhancements AS a
        JOIN detachment_enhancements AS e
          ON e.detachmentId = a.detachmentId AND e.enhancementId = a.enhancementId
        JOIN detachments AS d ON d.id = a.detachmentId
        JOIN roster_units AS u ON u.instanceId = a.rosterUnitInstanceId
        WHERE a.rosterId = :rosterId
        ORDER BY e.name
    """)
    fun observeAssignments(rosterId: String): Flow<List<AssignedEnhancementRow>>

    @Query("""
        SELECT e.* FROM detachment_enhancements AS e
        JOIN roster_detachments AS s ON s.detachmentId = e.detachmentId
        WHERE s.rosterId = :rosterId ORDER BY e.name
    """)
    fun observeAvailable(rosterId: String): Flow<List<DetachmentEnhancementEntity>>

    @Query("SELECT COUNT(*) FROM roster_enhancements WHERE rosterId = :rosterId AND detachmentId = :detachmentId")
    suspend fun countForDetachment(rosterId: String, detachmentId: String): Int

    @Query("SELECT COUNT(*) FROM roster_detachments WHERE rosterId = :rosterId AND detachmentId = :detachmentId")
    suspend fun countSelectedDetachment(rosterId: String, detachmentId: String): Int

    @Query("""
        SELECT * FROM detachment_enhancements
        WHERE detachmentId = :detachmentId AND enhancementId = :enhancementId LIMIT 1
    """)
    suspend fun findEnhancement(detachmentId: String, enhancementId: String): DetachmentEnhancementEntity?

    @Query("SELECT COUNT(*) FROM roster_enhancements WHERE rosterUnitInstanceId = :instanceId")
    suspend fun countForUnit(instanceId: String): Int

    @Insert
    suspend fun insert(row: RosterEnhancementEntity)

    @Query("""
        DELETE FROM roster_enhancements
        WHERE rosterId = :rosterId
          AND detachmentId = :detachmentId
          AND enhancementId = :enhancementId
    """)
    suspend fun delete(rosterId: String, detachmentId: String, enhancementId: String): Int
}
