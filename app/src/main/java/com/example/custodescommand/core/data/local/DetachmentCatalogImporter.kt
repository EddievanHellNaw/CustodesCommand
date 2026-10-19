package com.example.custodescommand.core.data.local

import android.content.Context
import androidx.room.withTransaction
import com.example.custodescommand.core.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Reference import only. Upserts detachments without deleting roster selections. */
class DetachmentCatalogImporter(
    private val context: Context,
    private val database: CustodesDatabase
) {
    suspend fun importIfNeeded(): Boolean {
        val json = withContext(Dispatchers.IO) {
            context.assets.open("rules/custodes_detachments_l8.json")
                .bufferedReader().use { JSONObject(it.readText()) }
        }
        val packId = json.getString("packId")
        val referenceDao = database.referenceDao()
        val dao = database.detachmentDao()
        return database.withTransaction {
            if (referenceDao.hasImported(packId)) return@withTransaction false
            val detachmentRows = mutableListOf<DetachmentEntity>()
            val stratagemRows = mutableListOf<DetachmentStratagemEntity>()
            val enhancementRows = mutableListOf<DetachmentEnhancementEntity>()
            val records = json.getJSONArray("detachments")
            for (i in 0 until records.length()) {
                val d = records.getJSONObject(i)
                val id = d.getString("id")
                val dp = d.getInt("dpCost")
                require(dp > 0) { "Invalid DP cost for $id" }
                detachmentRows.add(DetachmentEntity(
                    id = id, name = d.getString("name"), dpCost = dp,
                    dispositionsJson = d.getJSONArray("dispositions").toString(),
                    uniqueShieldHost = d.getBoolean("uniqueShieldHost"),
                    ruleTitle = d.getString("ruleTitle"),
                    ruleText = d.getString("ruleText"),
                    ruleTextStatus = d.getString("ruleTextStatus"),
                    favouredKatah = d.optNullable("favouredKatah"),
                    favouredKatahEffect = d.optNullable("favouredKatahEffect"),
                    sourcePage = d.getInt("sourcePage"),
                    extraSourcePagesJson = d.getJSONArray("additionalSourcePages").toString()
                ))
                val strats = d.getJSONArray("stratagems")
                for (s in 0 until strats.length()) {
                    val item = strats.getJSONObject(s)
                    stratagemRows.add(DetachmentStratagemEntity(
                        detachmentId = id, stratagemId = item.getString("id"),
                        name = item.getString("name"), cpCost = item.getInt("cpCost"),
                        phase = item.getString("phase"),
                        whenText = item.optNullable("whenText"),
                        targetText = item.optNullable("targetText"),
                        effectText = item.optNullable("effectText"),
                        textStatus = item.getString("textStatus"),
                        sourcePage = item.getInt("sourcePage")
                    ))
                }
                val enhancements = d.getJSONArray("enhancements")
                for (e in 0 until enhancements.length()) {
                    val item = enhancements.getJSONObject(e)
                    enhancementRows.add(DetachmentEnhancementEntity(
                        detachmentId = id, enhancementId = "${id}-e$e",
                        name = item.getString("name"), points = item.getInt("points"),
                        effectText = item.optNullable("effectText"),
                        eligibilityText = item.optNullable("eligibilityText"),
                        textStatus = item.getString("textStatus")
                    ))
                }
            }
            require(detachmentRows.map { it.id }.distinct().size == detachmentRows.size)
            dao.upsertDetachments(detachmentRows)
            // No roster_detachments rows are touched.
            // Do not delete and recreate referenced enhancements: saved roster
            // assignments now have foreign keys to their stable identifiers.
            dao.upsertStratagems(stratagemRows)
            dao.upsertEnhancements(enhancementRows)
            referenceDao.markImported(CatalogImportStateEntity(
                importId = packId, importedAtEpochMillis = System.currentTimeMillis()
            ))
            true
        }
    }
}
private fun JSONObject.optNullable(key: String): String? =
    if (!has(key) || isNull(key)) null else getString(key)
