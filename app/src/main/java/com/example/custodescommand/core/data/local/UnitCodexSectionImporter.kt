package com.example.custodescommand.core.data.local

import android.content.Context
import androidx.room.withTransaction
import com.example.custodescommand.core.data.local.entity.CatalogImportStateEntity
import com.example.custodescommand.core.data.local.entity.UnitCodexSectionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/**
 * Additive v6 import. Never deletes or replaces unit/weapon/wargear/points rows.
 * Leaving the old data in place is essential for persistent roster references.
 */
class UnitCodexSectionImporter(
    private val context: Context,
    private val database: CustodesDatabase
) {
    suspend fun importIfNeeded(): Boolean {
        val json = withContext(Dispatchers.IO) {
            context.assets.open("rules/codex_sections_l14_v1.json")
                .bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
        }
        val packId = json.getString("packId")
        val input = json.getJSONArray("sections")
        val sections = (0 until input.length()).map { i ->
            val row = input.getJSONObject(i)
            UnitCodexSectionEntity(
                unitId = row.getString("unitId"),
                kind = row.getString("kind"),
                text = row.getString("text"),
                sourcePage = if (row.isNull("sourcePage")) null else row.getInt("sourcePage"),
                status = row.getString("status")
            )
        }
        require(sections.map { it.unitId to it.kind }.distinct().size == sections.size) {
            "Duplicate unit/section in catalogue data."
        }
        return database.withTransaction {
            val dao = database.referenceDao()
            if (dao.hasImported(packId)) false else {
                // Fail, rather than silently creating orphan or guessed datasheets.
                for (id in sections.map { it.unitId }.distinct()) {
                    require(dao.findUnit(id) != null) { "Unknown unit ID in codex pack: $id" }
                }
                dao.upsertCodexSections(sections)
                dao.markImported(CatalogImportStateEntity(packId, System.currentTimeMillis()))
                true
            }
        }
    }
}
