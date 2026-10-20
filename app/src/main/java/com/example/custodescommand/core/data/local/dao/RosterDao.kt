
package com.example.custodescommand.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.custodescommand.core.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RosterDao {

    @Query("SELECT * FROM rosters ORDER BY name")
    fun observeRosters(): Flow<List<RosterEntity>>

    @Transaction
    @Query("SELECT * FROM rosters WHERE id = :id LIMIT 1")
    suspend fun getRosterWithUnits(
        id: String
    ): RosterWithUnits?

    // NEW: Observe the roster and its related units.
    @Transaction
    @Query("SELECT * FROM rosters WHERE id = :id LIMIT 1")
    fun observeRosterWithUnits(
        id: String
    ): Flow<RosterWithUnits?>

    @Query(
        "SELECT COALESCE(MAX(position), -1) + 1 " +
                "FROM roster_units WHERE rosterId = :rosterId"
    )
    suspend fun nextPosition(rosterId: String): Int

    @Insert
    suspend fun insertRoster(
        roster: RosterEntity
    )

    @Insert
    suspend fun insertRosterUnit(
        unit: RosterUnitEntity
    )

    // NEW: Remove one selected unit instance.
    @Query(
        "DELETE FROM roster_units " +
                "WHERE rosterId = :rosterId " +
                "AND instanceId = :instanceId"
    )
    suspend fun deleteRosterUnit(
        rosterId: String,
        instanceId: String
    ): Int

    @Query("DELETE FROM rosters WHERE id = :id")
    suspend fun deleteRoster(id: String)

    @Query("""
            UPDATE roster_units SET modelCount = :modelCount
            WHERE rosterId = :rosterId AND instanceId = :instanceId
        """)
    suspend fun changeModelCount(rosterId: String, instanceId: String, modelCount: Int): Int


}



