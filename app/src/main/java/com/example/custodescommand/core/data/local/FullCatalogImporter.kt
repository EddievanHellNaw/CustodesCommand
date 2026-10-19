package com.example.custodescommand.core.data.local

import android.content.Context
import androidx.room.withTransaction
import com.example.custodescommand.core.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Versioned offline reference import. Never deletes rows in units or roster tables.
 * All rule content stays in JSON assets so later lessons can add a file/update importer.
 */
class FullCatalogImporter(
    private val context: Context,
    private val database: CustodesDatabase
) {
    suspend fun importIfNeeded(): Boolean {
        val json = withContext(Dispatchers.IO) {
            context.assets.open("rules/custodes_catalog_v1_6.json")
                .bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
        }
        val importId = json.getString("packId")
        val dao = database.referenceDao()

        return database.withTransaction {
            if (dao.hasImported(importId)) return@withTransaction false

            val unitRows = mutableListOf<UnitEntity>()
            val profileRows = mutableListOf<UnitProfileEntity>()
            val priceRows = mutableListOf<PointTierEntity>()
            val weaponRows = mutableListOf<UnitWeaponEntity>()
            val abilityRows = mutableListOf<UnitAbilityEntity>()
            val wargearRows = mutableListOf<UnitWargearOptionEntity>()
            val optionalCosts = mutableListOf<OptionalWargearCostEntity>()

            val units = json.getJSONArray("units")
            for (index in 0 until units.length()) {
                val u = units.getJSONObject(index)
                val id = u.getString("id")
                val page = u.nullableInt("sourcePage")
                val sizes = u.getJSONArray("allowedModelCounts")
                    .stringsAsInts().toSet()

                unitRows += UnitEntity(
                    id = id,
                    name = u.getString("name"),
                    category = u.getString("category"),
                    allowedModelCounts = sizes,
                    sourceDocument = u.getString("sourceDocument"),
                    sourcePage = page
                )

                val stats = u.optJSONObject("characteristics")
                profileRows += UnitProfileEntity(
                    unitId = id,
                    playStatus = u.getString("playStatus"),
                    profileStatus = u.getString("profileStatus"),
                    movementInches = stats?.nullableInt("movementInches"),
                    toughness = stats?.nullableInt("toughness"),
                    armourSave = stats?.nullableString("armourSave"),
                    wounds = stats?.nullableInt("wounds"),
                    leadership = stats?.nullableString("leadership"),
                    objectiveControl = stats?.nullableInt("objectiveControl"),
                    invulnerableSave = stats?.nullableString("invulnerableSave"),
                    keywordsJson = u.getJSONArray("keywords").toString(),
                    leaderBodyguardIdsJson = u.getJSONArray("leaderBodyguardIds").toString(),
                    sourcePage = page
                )

                val prices = u.getJSONArray("points")
                for (p in 0 until prices.length()) {
                    val tier = prices.getJSONObject(p)
                    priceRows += PointTierEntity(
                        unitId = id,
                        modelCount = tier.getInt("modelCount"),
                        fromOccurrence = tier.getInt("fromOccurrence"),
                        throughOccurrence = tier.nullableInt("throughOccurrence"),
                        points = tier.getInt("points")
                    )
                }

                val weapons = u.getJSONArray("weapons")
                for (w in 0 until weapons.length()) {
                    val weapon = weapons.getJSONObject(w)
                    weaponRows += UnitWeaponEntity(
                        unitId = id, sortOrder = w,
                        name = weapon.getString("name"),
                        weaponType = weapon.getString("type"),
                        rangeText = weapon.getString("range"),
                        attacks = weapon.getString("attacks"),
                        skill = weapon.getString("skill"),
                        strength = weapon.getString("strength"),
                        armourPenetration = weapon.getInt("armourPenetration"),
                        damage = weapon.getString("damage"),
                        abilitiesJson = weapon.getJSONArray("abilities").toString()
                    )
                }

                val abilities = u.getJSONArray("abilities")
                for (a in 0 until abilities.length()) {
                    val ability = abilities.getJSONObject(a)
                    abilityRows += UnitAbilityEntity(
                        unitId = id, sortOrder = a,
                        name = ability.getString("name"),
                        ruleText = ability.getString("text")
                    )
                }

                val wargear = u.getJSONArray("wargearOptions")
                for (g in 0 until wargear.length()) {
                    wargearRows += UnitWargearOptionEntity(
                        unitId = id, sortOrder = g,
                        description = wargear.getString(g)
                    )
                }
            }

            val costs = json.getJSONArray("optionalWargearCosts")
            for (i in 0 until costs.length()) {
                val cost = costs.getJSONObject(i)
                optionalCosts += OptionalWargearCostEntity(
                    unitId = cost.getString("unitId"),
                    optionName = cost.getString("optionName"),
                    pointsPerItem = cost.getInt("pointsPerItem")
                )
            }

            // CRITICAL: Upsert unit definitions in-place. RosterUnitEntity references them.
            dao.upsertUnits(unitRows)
            dao.upsertMetadata(
                CatalogMetadataEntity(
                    id = "custodes",
                    pointsVersion = json.getString("pointsVersion")
                )
            )

            // Reference-only tables can be replaced together in one transaction.
            dao.clearPointTiers()
            dao.insertPointTiers(priceRows)
            dao.upsertProfiles(profileRows)
            dao.clearWeapons()
            dao.insertWeapons(weaponRows)
            dao.clearAbilities()
            dao.insertAbilities(abilityRows)
            dao.clearWargearOptions()
            dao.insertWargearOptions(wargearRows)
            dao.clearOptionalCosts()
            dao.insertOptionalCosts(optionalCosts)

            dao.markImported(CatalogImportStateEntity(
                importId = importId,
                importedAtEpochMillis = System.currentTimeMillis()
            ))
            true
        }
    }
}

private fun JSONObject.nullableInt(key: String): Int? =
    if (isNull(key)) null else getInt(key)

private fun JSONObject.nullableString(key: String): String? =
    if (isNull(key)) null else getString(key)

private fun JSONArray.stringsAsInts(): List<Int> =
    (0 until length()).map { getInt(it) }
