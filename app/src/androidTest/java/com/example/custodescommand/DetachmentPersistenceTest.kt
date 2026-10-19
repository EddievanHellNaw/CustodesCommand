package com.example.custodescommand

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.custodescommand.core.data.local.*
import com.example.custodescommand.core.data.repository.DetachmentRepository
import com.example.custodescommand.core.data.repository.RosterRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DetachmentPersistenceTest {
    private lateinit var context: Context
    private lateinit var database: CustodesDatabase
    private val name = "l8_detachments_test.db"

    private fun open() = Room.databaseBuilder(context, CustodesDatabase::class.java, name).build()

    @Before fun setup() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteDatabase(name)
        database = open()
    }
    @After fun teardown() {
        database.close()
        context.deleteDatabase(name)
    }

    @Test fun allDetachmentsAndStratagemsAreIndexed() = runBlocking {
        DetachmentCatalogImporter(context, database).importIfNeeded()
        DetachmentCatalogImporter(context, database).importIfNeeded() // Idempotent
        val repo = DetachmentRepository(database)
        val all = repo.observeAll().first()
        assertEquals(13, all.size)
        assertEquals(3, all.first { it.id == "guardians-throne" }.dpCost)
        var stratagemCount = 0
        var enhancementCount = 0
        all.forEach { d ->
            val detail = repo.findDetail(d.id)!!
            stratagemCount += detail.stratagems.size
            enhancementCount += detail.enhancements.size
        }
        assertEquals(41, stratagemCount)
        assertEquals(29, enhancementCount)
        assertEquals(2, repo.findDetail("guardians-throne")!!.sourcePages.size)
    }

    @Test fun rosterSelectionsSurviveReopening() = runBlocking {
        FullCatalogImporter(context, database).importIfNeeded()
        DetachmentCatalogImporter(context, database).importIfNeeded()
        val rosterId = RosterRepository(database).createRoster("Test detachments", 2000)
        var repo = DetachmentRepository(database)
        repo.setSelected(rosterId, "lions", true)
        repo.setSelected(rosterId, "guardians-throne", true)
        database.close()
        database = open()
        repo = DetachmentRepository(database)
        val selected = repo.observeSelectedIds(rosterId).first()
        assertEquals(setOf("lions", "guardians-throne"), selected)
        val definitions = repo.observeAll().first()
        assertEquals(4, definitions.filter { it.id in selected }.sumOf { it.dpCost })
    }
}
