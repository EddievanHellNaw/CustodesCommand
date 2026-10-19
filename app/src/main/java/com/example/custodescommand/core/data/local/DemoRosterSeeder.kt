package com.example.custodescommand.core.data.local

import android.content.Context
import com.example.custodescommand.core.data.repository.RosterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Developer-only examples. Call explicitly; never in production startup. */
class DemoRosterSeeder(
    private val context: Context,
    private val rosterRepository: RosterRepository
) {
    suspend fun seedIfMissing() {
        val data = withContext(Dispatchers.IO) {
            context.assets.open("rules/demo_rosters_v1.json")
                .bufferedReader(Charsets.UTF_8).use { JSONObject(it.readText()) }
        }
        val examples = data.getJSONArray("rosters")
        for (i in 0 until examples.length()) {
            val example = examples.getJSONObject(i)
            val name = example.getString("name")
            // Intended only for initial testing, not a robust resumable import protocol.
            if (rosterRepository.observeRosters().first().any { it.name == name }) continue
            val id = rosterRepository.createRoster(name, example.getInt("pointsLimit"))
            val units = example.getJSONArray("units")
            for (j in 0 until units.length()) {
                val unit = units.getJSONObject(j)
                rosterRepository.addUnit(id, unit.getString("unitId"), unit.getInt("modelCount"))
            }
        }
    }
}
