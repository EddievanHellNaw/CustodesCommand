package com.example.custodescommand

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.custodescommand.core.data.local.CustodesDatabase
import com.example.custodescommand.core.data.local.FullCatalogImporter
import com.example.custodescommand.core.data.local.UnitCodexSectionImporter
import com.example.custodescommand.core.data.local.CodexWeaponImporter
import com.example.custodescommand.core.data.local.WeaponReferenceImporter
import com.example.custodescommand.core.data.repository.UnitDetailRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Lesson14CodexImportTest {
    @Test
    fun importsCompositionsWithoutTouchingPointsOrLegends() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, CustodesDatabase::class.java).build()
        try {
            assertTrue(FullCatalogImporter(context, db).importIfNeeded())
            val unitCountBefore = db.referenceDao().unitCount()
            val tiersBefore = db.referenceDao().getPointTiers().size

            WeaponReferenceImporter(context, db).importIfNeeded()
            assertTrue(CodexWeaponImporter(context, db).importIfNeeded())
            assertFalse(CodexWeaponImporter(context, db).importIfNeeded())

            val importer = UnitCodexSectionImporter(context, db)
            assertTrue(importer.importIfNeeded())
            assertFalse(importer.importIfNeeded())

            assertEquals(unitCountBefore, db.referenceDao().unitCount())
            assertEquals(tiersBefore, db.referenceDao().getPointTiers().size)
            assertEquals(1, db.referenceDao().findCodexSections("allarus")
                .count { it.kind == "COMPOSITION" })

            val allarus = UnitDetailRepository(db.referenceDao()).findUnit("allarus")!!
            val currentWithScans = listOf(
                "allarus", "aquilon-gauntlets", "aquilon-talons", "blade-champion",
                "caladius-annihilator", "caladius", "achillus", "galatus", "coronus",
                "guard", "wardens", "gyrfalcon", "pallas", "sentinel", "shield-captain",
                "shield-captain-allarus", "shield-captain-jetbike", "telemon", "trajann",
                "venatari-kinetic", "venatari-lances", "vertus", "rhino",
                "knight-centura", "prosecutor", "vigilator", "witchseeker"
            )
            assertEquals(27, currentWithScans.size)
            currentWithScans.forEach { unitId ->
                assertTrue("Missing weapon profile for $unitId",
                    db.referenceDao().findWeapons(unitId).isNotEmpty())
                assertTrue("Missing composition for $unitId",
                    db.referenceDao().findCodexSections(unitId)
                        .any { it.kind == "COMPOSITION" })
            }
            assertTrue(allarus.codexSections.any { it.kind == "COMPOSITION" && "Allarus" in it.text })

            val legends = UnitDetailRepository(db.referenceDao()).findUnit("sagittarum")!!
            assertTrue(legends.codexSections.isEmpty())
            assertEquals("LEGENDS", legends.playStatus)

            val missingScan = db.referenceDao().findCodexSections("ares-gunship")
            assertEquals("SOURCE_MISSING", missingScan.first().status)
        } finally {
            db.close()
        }
    }
}
