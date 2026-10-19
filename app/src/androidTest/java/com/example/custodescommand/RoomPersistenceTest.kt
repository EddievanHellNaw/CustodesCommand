
package com.example.custodescommand

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.custodescommand.core.data.local.CatalogSeeder
import com.example.custodescommand.core.data.local.CustodesDatabase
import com.example.custodescommand.core.data.repository.ReferenceRepository
import com.example.custodescommand.core.data.repository.RosterRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomPersistenceTest {

    private lateinit var context: Context
    private lateinit var database: CustodesDatabase

    private val databaseName = "custodes_test.db"

    private fun openDatabase(): CustodesDatabase {
        return Room.databaseBuilder(
            context,
            CustodesDatabase::class.java,
            databaseName
        ).build()
    }

    @Before
    fun setup() {
        context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        context.deleteDatabase(databaseName)
        database = openDatabase()
    }

    @After
    fun cleanup() {
        database.close()
        context.deleteDatabase(databaseName)
    }

    @Test
    fun catalogIsSeededOnlyOnce() = runBlocking {

        val seeder = CatalogSeeder(database)

        seeder.seedIfEmpty()
        seeder.seedIfEmpty()

        val referenceRepository = ReferenceRepository(
            database.referenceDao()
        )

        val catalog = referenceRepository.loadPointsCatalog()

        assertEquals(
            4,
            database.referenceDao().unitCount()
        )

        assertEquals("MFM v1.6", catalog.version)
        assertEquals(12, catalog.tiers.size)
    }

    @Test
    fun rosterSurvivesDatabaseReopening() = runBlocking {

        CatalogSeeder(database).seedIfEmpty()

        val repository = RosterRepository(database)

        val rosterId = repository.createRoster(
            name = "Golden Talon",
            pointsLimit = 2000
        )

        repository.addUnit(
            rosterId = rosterId,
            unitId = "allarus",
            modelCount = 2
        )

        database.close()
        database = openDatabase()

        val restoredRoster = RosterRepository(database)
            .getRoster(rosterId)

        assertNotNull(restoredRoster)
        assertEquals("Golden Talon", restoredRoster?.name)
        assertEquals(1, restoredRoster?.units?.size)
        assertEquals(
            "allarus",
            restoredRoster?.units?.first()?.unitId
        )
    }
}
