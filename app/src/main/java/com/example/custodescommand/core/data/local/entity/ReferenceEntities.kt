
package com.example.custodescommand.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "catalog_metadata")
data class CatalogMetadataEntity(
    @PrimaryKey val id: String,
    val pointsVersion: String
)

@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val allowedModelCounts: Set<Int>,
    val sourceDocument: String?,
    val sourcePage: Int?
)

@Entity(
    tableName = "point_tiers",
    primaryKeys = [
        "unitId",
        "modelCount",
        "fromOccurrence"
    ],
    foreignKeys = [
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("unitId")]
)
data class PointTierEntity(
    val unitId: String,
    val modelCount: Int,
    val fromOccurrence: Int,
    val throughOccurrence: Int?,
    val points: Int
)
