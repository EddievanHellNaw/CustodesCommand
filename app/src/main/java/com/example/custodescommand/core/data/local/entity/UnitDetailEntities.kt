package com.example.custodescommand.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// One row for every indexed unit, even if no photographed datasheet was supplied.
@Entity(
    tableName = "unit_profiles",
    foreignKeys = [ForeignKey(
        entity = UnitEntity::class, parentColumns = ["id"], childColumns = ["unitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("unitId")]
)
data class UnitProfileEntity(
    @PrimaryKey val unitId: String,
    val playStatus: String,            // CURRENT or LEGENDS (source classification)
    val profileStatus: String,         // PARTIAL_TRANSCRIPTION or INDEX_ONLY
    val movementInches: Int?,
    val toughness: Int?,
    val armourSave: String?,
    val wounds: Int?,
    val leadership: String?,
    val objectiveControl: Int?,
    val invulnerableSave: String?,
    val keywordsJson: String,
    val leaderBodyguardIdsJson: String,
    val sourcePage: Int?
)

@Entity(
    tableName = "unit_weapons",
    primaryKeys = ["unitId", "sortOrder"],
    foreignKeys = [ForeignKey(
        entity = UnitEntity::class, parentColumns = ["id"], childColumns = ["unitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("unitId")]
)
data class UnitWeaponEntity(
    val unitId: String,
    val sortOrder: Int,
    val name: String,
    val weaponType: String,
    val rangeText: String,
    val attacks: String,
    val skill: String,
    val strength: String,
    val armourPenetration: Int,
    val damage: String,
    val abilitiesJson: String
)

@Entity(
    tableName = "unit_abilities",
    primaryKeys = ["unitId", "sortOrder"],
    foreignKeys = [ForeignKey(
        entity = UnitEntity::class, parentColumns = ["id"], childColumns = ["unitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("unitId")]
)
data class UnitAbilityEntity(
    val unitId: String,
    val sortOrder: Int,
    val name: String,
    val ruleText: String
)

@Entity(
    tableName = "unit_wargear_options",
    primaryKeys = ["unitId", "sortOrder"],
    foreignKeys = [ForeignKey(
        entity = UnitEntity::class, parentColumns = ["id"], childColumns = ["unitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("unitId")]
)
data class UnitWargearOptionEntity(
    val unitId: String,
    val sortOrder: Int,
    val description: String
)

@Entity(tableName = "catalog_import_state")
data class CatalogImportStateEntity(
    @PrimaryKey val importId: String,
    val importedAtEpochMillis: Long
)

@Entity(
    tableName = "optional_wargear_costs",
    primaryKeys = ["unitId", "optionName"],
    foreignKeys = [ForeignKey(
        entity = UnitEntity::class, parentColumns = ["id"], childColumns = ["unitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("unitId")]
)
data class OptionalWargearCostEntity(
    val unitId: String,
    val optionName: String,
    val pointsPerItem: Int
)
