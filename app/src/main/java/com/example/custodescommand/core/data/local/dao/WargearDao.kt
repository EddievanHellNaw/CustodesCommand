package com.example.custodescommand.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.example.custodescommand.core.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface WargearDao {
    @Upsert
    suspend fun upsertDefinitions(definitions: List<WargearDefinitionEntity>)

    @Query("SELECT COUNT(*) FROM catalog_import_state WHERE importId = :id")
    suspend fun hasImported(id: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markImported(mark: CatalogImportStateEntity)

    @Query("SELECT * FROM wargear_definitions WHERE unitId = :unitId ORDER BY name")
    fun observeOptions(unitId: String): Flow<List<WargearDefinitionEntity>>

    @Query("SELECT * FROM wargear_definitions WHERE unitId = :unitId ORDER BY name")
    suspend fun getOptions(unitId: String): List<WargearDefinitionEntity>

    @Query("SELECT * FROM wargear_definitions WHERE unitId = :unitId AND optionId = :optionId LIMIT 1")
    suspend fun findOption(unitId: String, optionId: String): WargearDefinitionEntity?

    @Query("""
        SELECT s.rosterUnitInstanceId, s.unitId, s.optionId, s.modelIndex,
               d.name, d.pointsPerSelection AS points, d.sourceStatus
        FROM roster_wargear_selections AS s
        JOIN wargear_definitions AS d ON d.unitId = s.unitId AND d.optionId = s.optionId
        JOIN roster_units AS r ON r.instanceId = s.rosterUnitInstanceId
        WHERE r.rosterId = :rosterId
        ORDER BY r.position, s.modelIndex, d.name
    """)
    fun observeRosterSelections(rosterId: String): Flow<List<WargearSelectionRow>>

    @Query("""
        SELECT s.rosterUnitInstanceId, s.unitId, s.optionId, s.modelIndex,
               d.name, d.pointsPerSelection AS points, d.sourceStatus
        FROM roster_wargear_selections AS s
        JOIN wargear_definitions AS d ON d.unitId = s.unitId AND d.optionId = s.optionId
        WHERE s.rosterUnitInstanceId = :instanceId
        ORDER BY s.modelIndex, d.name
    """)
    fun observeInstanceSelections(instanceId: String): Flow<List<WargearSelectionRow>>

    @Query("""
        SELECT s.rosterUnitInstanceId, s.unitId, s.optionId, s.modelIndex,
               d.name, d.pointsPerSelection AS points, d.sourceStatus
        FROM roster_wargear_selections AS s
        JOIN wargear_definitions AS d ON d.unitId = s.unitId AND d.optionId = s.optionId
        WHERE s.rosterUnitInstanceId = :instanceId
    """)
    suspend fun getInstanceSelections(instanceId: String): List<WargearSelectionRow>

    @Insert
    suspend fun insertSelection(selection: RosterWargearSelectionEntity)

    @Query("""
        DELETE FROM roster_wargear_selections
        WHERE rosterUnitInstanceId = :instanceId AND optionId = :optionId AND modelIndex = :modelIndex
    """)
    suspend fun deleteSelection(instanceId: String, optionId: String, modelIndex: Int): Int
    @Query("""
    DELETE FROM roster_wargear_selections
    WHERE rosterUnitInstanceId = :instanceId AND optionId = :optionId
""")
    suspend fun clearOptionSelections(instanceId: String, optionId: String): Int


}
