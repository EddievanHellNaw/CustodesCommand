package com.example.custodescommand.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Reference sections tied to an existing unit; never copied into saved roster rows. */
@Entity(
    tableName = "unit_codex_sections",
    primaryKeys = ["unitId", "kind"],
    foreignKeys = [ForeignKey(
        entity = UnitEntity::class,
        parentColumns = ["id"],
        childColumns = ["unitId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("unitId")]
)
data class UnitCodexSectionEntity(
    val unitId: String,
    val kind: String,
    val text: String,
    val sourcePage: Int?,
    val status: String
)
