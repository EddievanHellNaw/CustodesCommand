package com.example.custodescommand.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * This V1 assignment targets one roster-unit instance, not an individual miniature.
 * One enhancement per roster unit is a deliberate interim editor constraint.
 */
@Entity(
    tableName = "roster_enhancements",
    primaryKeys = ["rosterId", "detachmentId", "enhancementId"],
    foreignKeys = [
        ForeignKey(
            entity = RosterEntity::class,
            parentColumns = ["id"], childColumns = ["rosterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RosterUnitEntity::class,
            parentColumns = ["instanceId"], childColumns = ["rosterUnitInstanceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = RosterDetachmentEntity::class,
            parentColumns = ["rosterId", "detachmentId"],
            childColumns = ["rosterId", "detachmentId"],
            // Normal UI deselection is guarded by DetachmentRepository.
            // CASCADE also permits a whole roster to be deleted cleanly.
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DetachmentEnhancementEntity::class,
            parentColumns = ["detachmentId", "enhancementId"],
            childColumns = ["detachmentId", "enhancementId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("rosterId"),
        Index(value = ["rosterId", "detachmentId"]),
        Index(value = ["detachmentId", "enhancementId"]),
        Index(value = ["rosterUnitInstanceId"], unique = true)
    ]
)
data class RosterEnhancementEntity(
    val rosterId: String,
    val detachmentId: String,
    val enhancementId: String,
    val rosterUnitInstanceId: String
)

/** A relational query projection, not an independently stored table. */
data class AssignedEnhancementRow(
    val rosterId: String,
    val detachmentId: String,
    val enhancementId: String,
    val rosterUnitInstanceId: String,
    val unitId: String,
    val name: String,
    val points: Int,
    val textStatus: String,
    val eligibilityText: String?,
    val sourcePage: Int
)
