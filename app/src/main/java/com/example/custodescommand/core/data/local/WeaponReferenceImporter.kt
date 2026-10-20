package com.example.custodescommand.core.data.local

import android.content.Context
import androidx.room.withTransaction
import com.example.custodescommand.core.data.local.entity.CatalogImportStateEntity
import com.example.custodescommand.core.data.local.entity.UnitWeaponEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/** Adds only legible, manually transcribed profiles; existing weapons remain untouched. */
class WeaponReferenceImporter(
    private val context: Context,
    private val database: CustodesDatabase
) {
    suspend fun importIfNeeded(): Boolean {
        val pack = withContext(Dispatchers.IO) {
            context.assets.open("rules/weapon_profiles_l11_v1.json")
                .bufferedReader().use { JSONObject(it.readText()) }
        }
        val id = pack.getString("packId")
        val array = pack.getJSONArray("weapons")
        val weapons = (0 until array.length()).map { i ->
            val w = array.getJSONObject(i)
            val abilities = w.getJSONArray("abilities")
            UnitWeaponEntity(
                unitId = w.getString("unitId"),
                sortOrder = w.getInt("sortOrder"),
                name = w.getString("name"),
                weaponType = w.getString("weaponType"),
                rangeText = w.getString("range"),
                attacks = w.getString("attacks"),
                skill = w.getString("skill"),
                strength = w.getString("strength"),
                armourPenetration = w.getInt("armourPenetration"),
                damage = w.getString("damage"),
                abilitiesJson = JSONArray((0 until abilities.length()).map { abilities.getString(it) }).toString()
            )
        }
        require(weapons.map { it.unitId to it.sortOrder }.distinct().size == weapons.size)
        return database.withTransaction {
            val dao = database.referenceDao()
            if (dao.hasImported(id)) false
            else {
                dao.upsertWeapons(weapons)
                dao.markImported(CatalogImportStateEntity(id, System.currentTimeMillis()))
                true
            }
        }
    }
}
