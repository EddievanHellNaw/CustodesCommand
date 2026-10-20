package com.example.custodescommand.core

import android.content.Context
import androidx.room.Room
import com.example.custodescommand.core.data.local.CodexWeaponImporter
import com.example.custodescommand.core.data.local.CustodesDatabase
import com.example.custodescommand.core.data.local.FullCatalogImporter
import com.example.custodescommand.core.data.local.DetachmentCatalogImporter
import com.example.custodescommand.core.data.local.UnitCodexSectionImporter
import com.example.custodescommand.core.data.local.WargearCatalogImporter
import com.example.custodescommand.core.data.repository.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import com.example.custodescommand.core.data.media.UnitPhotoStore
import com.example.custodescommand.core.data.local.WeaponReferenceImporter
import com.example.custodescommand.core.rules.CoreRuleRepository
class AppContainer(private val context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext,
        CustodesDatabase::class.java,
        "custodes_command.db"
    ).build()

    val referenceRepository = ReferenceRepository(database.referenceDao())
    val rosterRepository = RosterRepository(database)
    val unitDetailRepository = UnitDetailRepository(database.referenceDao())
    val detachmentRepository = DetachmentRepository(database)
    val rosterEnhancementRepository = RosterEnhancementRepository(database)
    val wargearRepository = WargearRepository(database)
    val unitPhotoStore = UnitPhotoStore(context.applicationContext)
    val coreRulesRepository = CoreRuleRepository.from(context.applicationContext)
    private val initializationMutex = Mutex()
    private var initialized = false

    suspend fun initializeData() {
        initializationMutex.withLock {
            if (!initialized) {
                FullCatalogImporter(context, database).importIfNeeded()
                DetachmentCatalogImporter(context, database).importIfNeeded()
                WargearCatalogImporter(context, database).importIfNeeded()
                WeaponReferenceImporter(context, database).importIfNeeded()
                CodexWeaponImporter(context, database).importIfNeeded()
                UnitCodexSectionImporter(context, database).importIfNeeded()
                initialized = true
            }
        }
    }

    suspend fun installDemoRosters() {
        initializeData()
        com.example.custodescommand.core.data.local.DemoRosterSeeder(
            context, rosterRepository
        ).seedIfMissing()
    }
}
