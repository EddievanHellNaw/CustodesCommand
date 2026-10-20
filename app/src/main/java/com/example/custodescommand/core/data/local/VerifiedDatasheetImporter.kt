package com.example.custodescommand.core.data.local

import android.content.Context
import androidx.room.withTransaction
import com.example.custodescommand.core.data.local.entity.CatalogImportStateEntity
import com.example.custodescommand.core.data.local.entity.UnitAbilityEntity
import com.example.custodescommand.core.data.local.entity.UnitWargearOptionEntity
import com.example.custodescommand.core.data.local.entity.UnitWeaponEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Post-Lesson-14 reference-only update.
 * It preserves units, rosters, point tiers, enhancements, and saved wargear selections.
 * Existing detail data for each explicitly listed unit is replaced in a transaction.
 * Legends, Ares, and Orion are deliberately untouched.
 */
class VerifiedDatasheetImporter(
    private val context: Context,
    private val database: CustodesDatabase
) {
    suspend fun importIfNeeded(): Boolean {
        val pack = withContext(Dispatchers.IO) {
            context.assets.open("rules/custodes_verified_2026_10_20.json")
                .bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
        }
        val packId = pack.getString("packId")
        val rows = pack.getJSONArray("units")
        val ids = (0 until rows.length()).map { rows.getJSONObject(it).getString("unitId") }
        require(ids.distinct().size == ids.size) { "Duplicate unit IDs in verified pack" }

        return database.withTransaction {
            val dao = database.referenceDao()
            if (dao.hasImported(packId)) return@withTransaction false

            for (i in 0 until rows.length()) {
                val unit = rows.getJSONObject(i)
                val unitId = unit.getString("unitId")
                require(dao.findUnit(unitId) != null) { "Unknown unit: $unitId" }
                val oldProfile = requireNotNull(dao.findProfile(unitId)) {
                    "No stored profile for $unitId"
                }
                require(oldProfile.playStatus == "CURRENT") { "Refusing to replace Legends data" }

                val keywordArray = unit.getJSONArray("keywords")
                val keywords = (0 until keywordArray.length()).map { keywordArray.getString(it) }
                require(keywords.isNotEmpty()) { "Keywords missing for $unitId" }
                dao.upsertProfiles(listOf(oldProfile.copy(
                    keywordsJson = JSONArray(keywords).toString(),
                    profileStatus = "WEB_CROSS_CHECKED_RULE_SUMMARY"
                )))

                val abilitiesArray = unit.getJSONArray("abilities")
                val abilities = (0 until abilitiesArray.length()).map { j ->
                    val ability = abilitiesArray.getJSONObject(j)
                    UnitAbilityEntity(
                        unitId = unitId,
                        sortOrder = j,
                        name = ability.getString("name"),
                        ruleText = ability.getString("text")
                    )
                }
                require(abilities.isNotEmpty()) { "Ability list missing for $unitId" }
                dao.deleteVerifiedAbilities(unitId)
                dao.insertAbilities(abilities)

                val wargearArray = unit.getJSONArray("wargearOptions")
                val descriptions = (0 until wargearArray.length()).map { j ->
                    UnitWargearOptionEntity(
                        unitId = unitId,
                        sortOrder = j,
                        description = wargearArray.getString(j)
                    )
                }
                dao.deleteVerifiedWargearDescriptions(unitId)
                if (descriptions.isNotEmpty()) dao.insertWargearOptions(descriptions)

                val weaponsArray = unit.getJSONArray("weapons")
                val weapons = (0 until weaponsArray.length()).map { j ->
                    val weapon = weaponsArray.getJSONObject(j)
                    val effects = weapon.getJSONArray("abilities")
                    UnitWeaponEntity(
                        unitId = unitId,
                        sortOrder = j,
                        name = weapon.getString("name"),
                        weaponType = weapon.getString("weaponType"),
                        rangeText = weapon.getString("range"),
                        attacks = weapon.getString("attacks"),
                        skill = weapon.getString("skill"),
                        strength = weapon.getString("strength"),
                        armourPenetration = weapon.getInt("armourPenetration"),
                        damage = weapon.getString("damage"),
                        abilitiesJson = effects.toString()
                    )
                }
                require(weapons.isNotEmpty()) { "No verified weapons for $unitId" }
                dao.deleteVerifiedWeapons(unitId)
                dao.insertWeapons(weapons)
            }
            dao.markImported(CatalogImportStateEntity(packId, System.currentTimeMillis()))
            true
        }
    }
}
