package com.example.custodescommand.core.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.custodescommand.core.data.local.dao.*
import com.example.custodescommand.core.data.local.entity.*

@Database(
    entities = [
        CatalogMetadataEntity::class, UnitEntity::class, PointTierEntity::class,
        RosterEntity::class, RosterUnitEntity::class,
        UnitProfileEntity::class, UnitWeaponEntity::class,
        UnitAbilityEntity::class, UnitWargearOptionEntity::class,
        CatalogImportStateEntity::class, OptionalWargearCostEntity::class,
        DetachmentEntity::class, DetachmentStratagemEntity::class,
        DetachmentEnhancementEntity::class, RosterDetachmentEntity::class
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ]
)
@TypeConverters(Converters::class)
abstract class CustodesDatabase : RoomDatabase() {
    abstract fun referenceDao(): ReferenceDao
    abstract fun rosterDao(): RosterDao
    abstract fun detachmentDao(): DetachmentDao
}
