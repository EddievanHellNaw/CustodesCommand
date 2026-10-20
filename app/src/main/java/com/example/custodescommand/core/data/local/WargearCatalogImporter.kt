package com.example.custodescommand.core.data.local

import android.content.Context
import androidx.room.withTransaction
import com.example.custodescommand.core.data.local.entity.CatalogImportStateEntity
import com.example.custodescommand.core.data.local.entity.WargearDefinitionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Additive, versioned, repeatable reference imports; never deletes saved selections. */
class WargearCatalogImporter(
    private val context: Context,
    private val database: CustodesDatabase
) {
    suspend fun importIfNeeded(): Boolean {
        var changed = false
        for (filename in listOf(
            "rules/wargear_options_l10_v1.json",
            "rules/wargear_options_l11_v1.json"
        )) {
            val pack = withContext(Dispatchers.IO) {
                context.assets.open(filename).bufferedReader().use { JSONObject(it.readText()) }
            }
            val id = pack.getString("packId")
            val rows = pack.getJSONArray("options")
            val options = (0 until rows.length()).map { index ->
                val o = rows.getJSONObject(index)
                WargearDefinitionEntity(
                    unitId = o.getString("unitId"),
                    optionId = o.getString("optionId"),
                    name = o.getString("name"),
                    pointsPerSelection = o.getInt("pointsPerSelection"),
                    maxPerUnit = if (o.isNull("maxPerUnit")) null else o.getInt("maxPerUnit"),
                    exclusivityGroup = if (o.isNull("exclusivityGroup")) null else o.getString("exclusivityGroup"),
                    sourceStatus = o.getString("sourceStatus"),
                    sourcePage = if (o.isNull("sourcePage")) null else o.getInt("sourcePage"),
                    description = o.getString("description")
                )
            }
            require(options.map { it.unitId to it.optionId }.distinct().size == options.size) {
                "Duplicate (unitId, optionId) in $filename"
            }
            require(options.all { it.pointsPerSelection >= 0 && (it.maxPerUnit == null || it.maxPerUnit > 0) }) {
                "Invalid option cost/limit in $filename"
            }
            changed = database.withTransaction {
                val dao = database.wargearDao()
                if (dao.hasImported(id) > 0) false
                else {
                    dao.upsertDefinitions(options)
                    dao.markImported(CatalogImportStateEntity(id, System.currentTimeMillis()))
                    true
                }
            } || changed
        }
        return changed
    }
}
