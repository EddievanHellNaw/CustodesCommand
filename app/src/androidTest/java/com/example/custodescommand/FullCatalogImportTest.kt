package com.example.custodescommand

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.custodescommand.core.data.local.CustodesDatabase
import com.example.custodescommand.core.data.local.FullCatalogImporter
import com.example.custodescommand.core.data.local.DemoRosterSeeder
import com.example.custodescommand.core.data.repository.ReferenceRepository
import com.example.custodescommand.core.data.repository.RosterRepository
import com.example.custodescommand.core.data.repository.UnitDetailRepository
import com.example.custodescommand.core.domain.service.PointsCalculator
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FullCatalogImportTest {
    @Test
    fun importsCompleteIndexAndDemoRosters() = runBlocking {
        val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, CustodesDatabase::class.java).build()
        try {
            val importer = FullCatalogImporter(context, db)
            assertTrue(importer.importIfNeeded())
            assertFalse(importer.importIfNeeded()) // no duplicate import

            val dao = db.referenceDao()
            val reference = ReferenceRepository(dao)
            val units = reference.observeUnits().first()
            val points = reference.loadPointsCatalog()

            assertEquals(36, units.size)
            assertEquals(74, points.tiers.size)
            assertEquals("MFM v1.6", points.version)

            val detail = UnitDetailRepository(dao).findUnit("allarus")
            assertNotNull(detail)
            assertEquals(20, detail?.sourcePage)
            assertEquals(5, detail?.weapons?.size)
            assertEquals("LEGENDS", UnitDetailRepository(dao).findUnit("sagittarum")?.playStatus)
            assertEquals(25, UnitDetailRepository(dao)
                .findUnit("shield-captain")?.extraWargearCosts?.first()?.pointsPerItem)

            val rosters = RosterRepository(db)
            DemoRosterSeeder(context, rosters).seedIfMissing()
            val demoRosters = rosters.observeRosters().first()
            assertEquals(2, demoRosters.size)
            val calculator = PointsCalculator(points)
            demoRosters.forEach { summary ->
                val loaded = rosters.getRoster(summary.id)!!
                assertEquals(summary.pointsLimit, calculator.calculate(loaded).totalPoints)
            }
        } finally {
            db.close()
        }
    }
}
