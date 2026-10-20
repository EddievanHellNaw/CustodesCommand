package com.example.custodescommand.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import com.example.custodescommand.core.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReferenceDao {
    @Query("SELECT COUNT(*) FROM units")
    suspend fun unitCount(): Int
    @Query("SELECT * FROM units ORDER BY name")
    fun observeUnits(): Flow<List<UnitEntity>>
    @Query("SELECT * FROM units WHERE id = :id LIMIT 1")
    suspend fun findUnit(id: String): UnitEntity?
    @Query("SELECT * FROM point_tiers")
    suspend fun getPointTiers(): List<PointTierEntity>
    @Query("SELECT * FROM catalog_metadata WHERE id = 'custodes' LIMIT 1")
    suspend fun getMetadata(): CatalogMetadataEntity?

    // Keep compatibility with the Lesson 4 seeder and its existing tests.
    @Insert suspend fun insertMetadata(metadata: CatalogMetadataEntity)
    @Insert suspend fun insertUnits(units: List<UnitEntity>)
    @Insert suspend fun insertPointTiers(tiers: List<PointTierEntity>)

    // Versioned Lesson 7 import.
    @Upsert suspend fun upsertUnits(units: List<UnitEntity>)
    @Upsert suspend fun upsertMetadata(metadata: CatalogMetadataEntity)
    @Query("DELETE FROM point_tiers") suspend fun clearPointTiers()
    @Upsert suspend fun upsertProfiles(profiles: List<UnitProfileEntity>)
    @Query("DELETE FROM unit_weapons") suspend fun clearWeapons()
    @Insert suspend fun insertWeapons(weapons: List<UnitWeaponEntity>)
    @Query("DELETE FROM unit_abilities") suspend fun clearAbilities()
    @Insert suspend fun insertAbilities(abilities: List<UnitAbilityEntity>)
    @Query("DELETE FROM unit_wargear_options") suspend fun clearWargearOptions()
    @Insert suspend fun insertWargearOptions(options: List<UnitWargearOptionEntity>)
    @Query("DELETE FROM optional_wargear_costs") suspend fun clearOptionalCosts()
    @Insert suspend fun insertOptionalCosts(costs: List<OptionalWargearCostEntity>)
    @Query("SELECT EXISTS(SELECT 1 FROM catalog_import_state WHERE importId = :importId)")
    suspend fun hasImported(importId: String): Boolean
    @Insert suspend fun markImported(state: CatalogImportStateEntity)

    @Query("SELECT * FROM unit_profiles WHERE unitId = :unitId LIMIT 1")
    suspend fun findProfile(unitId: String): UnitProfileEntity?
    @Query("SELECT * FROM unit_weapons WHERE unitId = :unitId ORDER BY sortOrder")
    suspend fun findWeapons(unitId: String): List<UnitWeaponEntity>
    @Query("SELECT * FROM unit_abilities WHERE unitId = :unitId ORDER BY sortOrder")
    suspend fun findAbilities(unitId: String): List<UnitAbilityEntity>
    @Query("SELECT * FROM unit_wargear_options WHERE unitId = :unitId ORDER BY sortOrder")
    suspend fun findWargearOptions(unitId: String): List<UnitWargearOptionEntity>
    @Query("SELECT * FROM point_tiers WHERE unitId = :unitId ORDER BY modelCount, fromOccurrence")
    suspend fun findPointTiers(unitId: String): List<PointTierEntity>
    @Query("SELECT * FROM optional_wargear_costs WHERE unitId = :unitId")
    suspend fun findOptionalWargearCosts(unitId: String): List<OptionalWargearCostEntity>
    // Upserts verified weapon profiles from the L11 incremental reference pack.
    @Upsert
    suspend fun upsertWeapons(weapons: List<UnitWeaponEntity>)

    @Upsert
    suspend fun upsertCodexSections(sections: List<UnitCodexSectionEntity>)

    @Query("SELECT * FROM unit_codex_sections WHERE unitId = :unitId ORDER BY kind")
    suspend fun findCodexSections(unitId: String): List<UnitCodexSectionEntity>
    @Query("DELETE FROM unit_abilities WHERE unitId = :unitId")
    suspend fun deleteVerifiedAbilities(unitId: String)

    @Query("DELETE FROM unit_wargear_options WHERE unitId = :unitId")
    suspend fun deleteVerifiedWargearDescriptions(unitId: String)

    @Query("DELETE FROM unit_weapons WHERE unitId = :unitId")
    suspend fun deleteVerifiedWeapons(unitId: String)


}
