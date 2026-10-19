package com.example.custodescommand.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.custodescommand.core.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DetachmentDao {
    @Query("SELECT * FROM detachments ORDER BY name")
    fun observeDetachments(): Flow<List<DetachmentEntity>>

    @Query("SELECT * FROM detachments WHERE id = :id LIMIT 1")
    suspend fun findDetachment(id: String): DetachmentEntity?

    @Query("SELECT * FROM detachment_stratagems WHERE detachmentId = :id ORDER BY name")
    suspend fun findStratagems(id: String): List<DetachmentStratagemEntity>

    @Query("SELECT * FROM detachment_enhancements WHERE detachmentId = :id ORDER BY name")
    suspend fun findEnhancements(id: String): List<DetachmentEnhancementEntity>

    @Query("SELECT * FROM roster_detachments WHERE rosterId = :rosterId ORDER BY detachmentId")
    fun observeRosterSelections(rosterId: String): Flow<List<RosterDetachmentEntity>>

    @Upsert
    suspend fun upsertDetachments(rows: List<DetachmentEntity>)

    @Query("DELETE FROM detachment_stratagems")
    suspend fun clearStratagems()

    @Insert
    suspend fun insertStratagems(rows: List<DetachmentStratagemEntity>)

    @Query("DELETE FROM detachment_enhancements")
    suspend fun clearEnhancements()

    @Insert
    suspend fun insertEnhancements(rows: List<DetachmentEnhancementEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun selectForRoster(row: RosterDetachmentEntity)

    @Query("DELETE FROM roster_detachments WHERE rosterId = :rosterId AND detachmentId = :detachmentId")
    suspend fun deselectForRoster(rosterId: String, detachmentId: String)
}
