package com.example.custodescommand.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** New reference definitions: the existing UnitWargearOptionEntity is descriptive text only. */
@Entity(
    tableName = "wargear_definitions",
    primaryKeys = ["unitId", "optionId"],
    foreignKeys = [ForeignKey(
        entity = UnitEntity::class,
        parentColumns = ["id"], childColumns = ["unitId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("unitId")]
)
data class WargearDefinitionEntity(
    val unitId: String,
    val optionId: String,
    val name: String,
    val pointsPerSelection: Int,
    val maxPerUnit: Int?,
    val exclusivityGroup: String?,
    val sourceStatus: String,
    val sourcePage: Int?,
    val description: String
)

/** A row describes an option equipped by one physical miniature in a roster squad. */
@Entity(
    tableName = "roster_wargear_selections",
    primaryKeys = ["rosterUnitInstanceId", "optionId", "modelIndex"],
    foreignKeys = [
        ForeignKey(
            entity = RosterUnitEntity::class,
            parentColumns = ["instanceId"], childColumns = ["rosterUnitInstanceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = WargearDefinitionEntity::class,
            parentColumns = ["unitId", "optionId"], childColumns = ["unitId", "optionId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("rosterUnitInstanceId"), Index(value = ["unitId", "optionId"])]
)
data class RosterWargearSelectionEntity(
    val rosterUnitInstanceId: String,
    val unitId: String,
    val optionId: String,
    val modelIndex: Int
)

/** DAO read projection. The authoritative cost comes from the reference option. */
data class WargearSelectionRow(
    val rosterUnitInstanceId: String,
    val unitId: String,
    val optionId: String,
    val modelIndex: Int,
    val name: String,
    val points: Int,
    val sourceStatus: String
)
