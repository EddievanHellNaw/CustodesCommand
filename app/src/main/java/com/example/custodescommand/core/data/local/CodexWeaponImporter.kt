package com.example.custodescommand.core.data.local

import android.content.Context
import androidx.room.withTransaction
import com.example.custodescommand.core.data.local.entity.CatalogImportStateEntity
import com.example.custodescommand.core.data.local.entity.UnitWeaponEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Imports only verified additional weapon rows; never clears existing profiles. */
class CodexWeaponImporter(
    private val context: Context,
    private val database: CustodesDatabase
) {
    suspend fun importIfNeeded(): Boolean {
        val json = withContext(Dispatchers.IO) {
            context.assets.open("rules/weapon_profiles_l14_v1.json")
                .bufferedReader().use { JSONObject(it.readText()) }
        }
        val id = json.getString("packId")
        val source = json.getJSONArray("weapons")
        val rows = (0 until source.length()).map { i ->
            val w = source.getJSONObject(i)
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
                abilitiesJson = w.getJSONArray("abilities").toString()
            )
        }
        require(rows.map { it.unitId to it.sortOrder }.distinct().size == rows.size)
        return database.withTransaction {
            val dao = database.referenceDao()
            if (dao.hasImported(id)) false else {
                for (unit in rows.map { it.unitId }.distinct()) {
                    require(dao.findUnit(unit) != null) { "Unknown unit ID: $unit" }
                }
                dao.upsertWeapons(rows) // Existing from Lesson 11
                dao.markImported(CatalogImportStateEntity(id, System.currentTimeMillis()))
                true
            }
        }
    }
}
