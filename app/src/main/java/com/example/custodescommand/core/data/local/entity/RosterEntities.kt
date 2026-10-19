
package com.example.custodescommand.core.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "rosters")
data class RosterEntity(
    @PrimaryKey val id: String,
    val name: String,
    val pointsLimit: Int
)

@Entity(
    tableName = "roster_units",
    foreignKeys = [
        ForeignKey(
            entity = RosterEntity::class,
            parentColumns = ["id"],
            childColumns = ["rosterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("rosterId"),
        Index("unitId")
    ]
)
data class RosterUnitEntity(
    @PrimaryKey val instanceId: String,
    val rosterId: String,
    val unitId: String,
    val modelCount: Int,
    val position: Int
)

data class RosterWithUnits(
    @Embedded val roster: RosterEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "rosterId"
    )
    val units: List<RosterUnitEntity>
)
