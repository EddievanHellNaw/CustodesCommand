package com.example.custodescommand

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.custodescommand.core.data.local.*
import com.example.custodescommand.core.data.repository.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RosterWargearPersistenceTest {
    private lateinit var context: Context
    private lateinit var db: CustodesDatabase
    private lateinit var rosters: RosterRepository
    private lateinit var equipment: WargearRepository
    private val dbName = "lesson10_test.db"

    private fun open() = Room.databaseBuilder(context, CustodesDatabase::class.java, dbName).build()

    @Before fun setup() = runBlocking {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(dbName)
        db = open()
        FullCatalogImporter(context, db).importIfNeeded()
        WargearCatalogImporter(context, db).importIfNeeded()
        rosters = RosterRepository(db)
        equipment = WargearRepository(db)
    }

    @After fun cleanup() {
        db.close()
        context.deleteDatabase(dbName)
    }

    private suspend fun newUnit(unitId: String, size: Int): Pair<String, String> {
        val rosterId = rosters.createRoster("Wargear test", 2000)
        rosters.addUnit(rosterId, unitId, size)
        return rosterId to rosters.getRoster(rosterId)!!.units.single().instanceId
    }

    @Test fun importingTwiceDoesNotDuplicateOptions() = runBlocking {
        assertFalse(WargearCatalogImporter(context, db).importIfNeeded())
        assertEquals(2, db.wargearDao().getOptions("allarus").size)
        assertEquals(1, db.wargearDao().getOptions("shield-captain").size)
    }

    @Test fun paidPraesidiumShieldPersistsAndAdds25Points() = runBlocking {
        val (rosterId, instanceId) = newUnit("shield-captain", 1)
        equipment.select(rosterId, instanceId, "praesidium-shield", 1, true)
        assertEquals(25, equipment.observeRosterSelections(rosterId).first().sumOf { it.points })
        db.close()
        db = open()
        assertEquals(25, WargearRepository(db).observeRosterSelections(rosterId).first().sumOf { it.points })
    }

    @Test fun onlyOneVexillaIsAllowedPerAllarusSquad() = runBlocking {
        val (rosterId, instanceId) = newUnit("allarus", 3)
        equipment.select(rosterId, instanceId, "vexilla", 1, true)
        val failure = runCatching {
            equipment.select(rosterId, instanceId, "vexilla", 2, true)
        }.exceptionOrNull()
        assertTrue(failure is IllegalArgumentException)
        assertEquals(1, equipment.observeInstanceSelections(instanceId).first().size)
    }

    @Test fun knightCenturaWeaponAlternativesAreExclusive() = runBlocking {
        val (rosterId, instanceId) = newUnit("knight-centura", 1)
        equipment.select(rosterId, instanceId, "master-crafted-boltgun", 1, true)
        val failure = runCatching {
            equipment.select(rosterId, instanceId, "master-crafted-flamer", 1, true)
        }.exceptionOrNull()
        assertTrue(failure is IllegalArgumentException)
    }

    @Test fun resizingCannotDiscardEquipmentFromRemovedModel() = runBlocking {
        val (rosterId, instanceId) = newUnit("allarus", 3)
        equipment.select(rosterId, instanceId, "castellan-axe", 3, true)
        val failure = runCatching { equipment.changeModelCount(rosterId, instanceId, 2) }.exceptionOrNull()
        assertTrue(failure is IllegalArgumentException)
        assertEquals(3, rosters.getRoster(rosterId)!!.units.single().modelCount)
        equipment.select(rosterId, instanceId, "castellan-axe", 3, false)
        equipment.changeModelCount(rosterId, instanceId, 2)
        assertEquals(2, rosters.getRoster(rosterId)!!.units.single().modelCount)
    }

    @Test fun removingSquadCascadesEquipment() = runBlocking {
        val (rosterId, instanceId) = newUnit("shield-captain", 1)
        equipment.select(rosterId, instanceId, "praesidium-shield", 1, true)
        rosters.removeUnit(rosterId, instanceId)
        assertTrue(equipment.observeRosterSelections(rosterId).first().isEmpty())
    }

    @Test fun optionForDifferentDatasheetCannotBeSelected() = runBlocking {
        val (rosterId, instanceId) = newUnit("allarus", 2)
        val failure = runCatching {
            equipment.select(rosterId, instanceId, "praesidium-shield", 1, true)
        }.exceptionOrNull()
        assertTrue(failure is IllegalArgumentException)
    }
}
