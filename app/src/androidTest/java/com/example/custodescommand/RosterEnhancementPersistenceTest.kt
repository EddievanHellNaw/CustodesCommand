package com.example.custodescommand

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.custodescommand.core.data.local.*
import com.example.custodescommand.core.data.repository.*
import com.example.custodescommand.core.domain.service.PointsCalculator
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RosterEnhancementPersistenceTest {
    private lateinit var context: Context
    private lateinit var db: CustodesDatabase
    private lateinit var rosters: RosterRepository
    private lateinit var detachments: DetachmentRepository
    private lateinit var enhancements: RosterEnhancementRepository
    private lateinit var references: ReferenceRepository
    private val fileName = "enhancement_lesson9_test.db"

    @Before fun setup() = runBlocking {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(fileName)
        db = Room.databaseBuilder(context, CustodesDatabase::class.java, fileName).build()
        FullCatalogImporter(context, db).importIfNeeded()
        DetachmentCatalogImporter(context, db).importIfNeeded()
        rosters = RosterRepository(db)
        detachments = DetachmentRepository(db)
        enhancements = RosterEnhancementRepository(db)
        references = ReferenceRepository(db.referenceDao())
    }

    @After fun cleanup() {
        db.close()
        context.deleteDatabase(fileName)
    }

    @Test fun assignedEnhancementAddsPointsAndSurvivesReopening() = runBlocking {
        val rosterId = rosters.createRoster("Terminator Test", 2000)
        rosters.addUnit(rosterId, "shield-captain-allarus", 1)
        val unit = rosters.getRoster(rosterId)!!.units.single()
        detachments.setSelected(rosterId, "lions", true)
        enhancements.assign(rosterId, "lions", "lions-e0", unit.instanceId)

        val first = enhancements.observeAssignments(rosterId).first()
        assertEquals(1, first.size)
        assertEquals(20, first.single().points)

        val base = PointsCalculator(references.loadPointsCatalog())
            .calculate(rosters.getRoster(rosterId)!!).totalPoints
        assertEquals(base + 20, base + first.sumOf { it.points })

        db.close()
        db = Room.databaseBuilder(context, CustodesDatabase::class.java, fileName).build()
        assertEquals(1, RosterEnhancementRepository(db).observeAssignments(rosterId).first().size)
    }

    @Test fun cannotAssignFromUnselectedDetachment() = runBlocking {
        val rosterId = rosters.createRoster("Unselected Detachment Test", 2000)
        rosters.addUnit(rosterId, "shield-captain-allarus", 1)
        val instanceId = rosters.getRoster(rosterId)!!.units.single().instanceId
        val error = runCatching {
            enhancements.assign(rosterId, "lions", "lions-e0", instanceId)
        }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        assertTrue(enhancements.observeAssignments(rosterId).first().isEmpty())
    }

    @Test fun preventsDetachmentRemovalWhileEnhancementAssigned() = runBlocking {
        val rosterId = rosters.createRoster("Prevent Data Loss", 2000)
        rosters.addUnit(rosterId, "shield-captain-allarus", 1)
        val id = rosters.getRoster(rosterId)!!.units.single().instanceId
        detachments.setSelected(rosterId, "lions", true)
        enhancements.assign(rosterId, "lions", "lions-e0", id)
        val error = runCatching { detachments.setSelected(rosterId, "lions", false) }.exceptionOrNull()
        assertTrue(error is IllegalArgumentException)
        assertEquals(setOf("lions"), detachments.observeSelectedIds(rosterId).first())
    }

    @Test fun deletingSquadCascadesToEnhancementAssignment() = runBlocking {
        val rosterId = rosters.createRoster("Squad Cascade", 2000)
        rosters.addUnit(rosterId, "shield-captain-allarus", 1)
        val unitId = rosters.getRoster(rosterId)!!.units.single().instanceId
        detachments.setSelected(rosterId, "lions", true)
        enhancements.assign(rosterId, "lions", "lions-e0", unitId)
        rosters.removeUnit(rosterId, unitId)
        assertTrue(enhancements.observeAssignments(rosterId).first().isEmpty())
    }

    @Test fun sourceRestrictionPreventsLionsEnhancementOnAllarusSquad() = runBlocking {
        val rosterId = rosters.createRoster("Wrong Target", 2000)
        rosters.addUnit(rosterId, "allarus", 2)
        val instanceId = rosters.getRoster(rosterId)!!.units.single().instanceId
        detachments.setSelected(rosterId, "lions", true)
        val err = runCatching {
            enhancements.assign(rosterId, "lions", "lions-e0", instanceId)
        }.exceptionOrNull()
        assertTrue(err is IllegalArgumentException)
    }
}
