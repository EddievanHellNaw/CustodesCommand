package com.example.custodescommand

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.custodescommand.core.data.local.*
import com.example.custodescommand.core.data.repository.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Isolated on-disk DB; proves that a Vexilla is one PER SQUAD, not one per model. */
@RunWith(AndroidJUnit4::class)
class VexillaPersistenceTest {
    @Test
    fun vexillaBearerMovesAtomicallyAndSurvivesReopen() = runBlocking {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val filename = "test_l11_vexilla.db"
        context.deleteDatabase(filename)
        var db = Room.databaseBuilder(context, CustodesDatabase::class.java, filename).build()
        try {
            // The existing catalogue must be installed before the incremental L10/L11 wargear packs.
            FullCatalogImporter(context, db).importIfNeeded()
            WargearCatalogImporter(context, db).importIfNeeded()
            val rosterId = RosterRepository(db).createRoster("Vexilla test", 2000)
            RosterRepository(db).addUnit(rosterId, "allarus", 3)
            val instanceId = RosterRepository(db).getRoster(rosterId)!!.units.single().instanceId
            val repo = WargearRepository(db)
            repo.setVexillaBearer(rosterId, instanceId, 1)
            assertEquals(listOf(1), repo.observeInstanceSelections(instanceId).first()
                .filter { it.optionId == "vexilla" }.map { it.modelIndex })
            repo.setVexillaBearer(rosterId, instanceId, 3)
            assertEquals(listOf(3), repo.observeInstanceSelections(instanceId).first()
                .filter { it.optionId == "vexilla" }.map { it.modelIndex })
            db.close()
            db = Room.databaseBuilder(context, CustodesDatabase::class.java, filename).build()
            assertEquals(listOf(3), WargearRepository(db).observeInstanceSelections(instanceId).first()
                .filter { it.optionId == "vexilla" }.map { it.modelIndex })
        } finally {
            db.close()
            context.deleteDatabase(filename)
        }
    }

    @Test
    fun secondVexillaDirectSelectIsRejectedAndOtherSquadIsAllowed() = runBlocking {
        val ctx: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(ctx, CustodesDatabase::class.java).build()
        try {
            FullCatalogImporter(ctx, db).importIfNeeded()
            WargearCatalogImporter(ctx, db).importIfNeeded()
            val rosters = RosterRepository(db)
            val rosterId = rosters.createRoster("Two squads", 2000)
            rosters.addUnit(rosterId, "allarus", 3)
            rosters.addUnit(rosterId, "allarus", 3)
            val (a, b) = rosters.getRoster(rosterId)!!.units.map { it.instanceId }
            val repo = WargearRepository(db)
            repo.select(rosterId, a, "vexilla", 1, true)
            var rejected = false
            try { repo.select(rosterId, a, "vexilla", 2, true) }
            catch (_: IllegalArgumentException) { rejected = true }
            assertTrue(rejected)
            repo.select(rosterId, b, "vexilla", 2, true)
            assertEquals(1, repo.observeInstanceSelections(a).first().count { it.optionId == "vexilla" })
            assertEquals(1, repo.observeInstanceSelections(b).first().count { it.optionId == "vexilla" })
        } finally { db.close() }
    }
}
