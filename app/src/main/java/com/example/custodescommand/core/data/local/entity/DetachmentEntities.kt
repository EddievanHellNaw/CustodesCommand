package com.example.custodescommand.core.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "detachments")
data class DetachmentEntity(
    @androidx.room.PrimaryKey val id: String,
    val name: String,
    val dpCost: Int,
    val dispositionsJson: String,
    val uniqueShieldHost: Boolean,
    val ruleTitle: String,
    val ruleText: String,
    val ruleTextStatus: String,
    val favouredKatah: String?,
    val favouredKatahEffect: String?,
    val sourcePage: Int,
    val extraSourcePagesJson: String
)

@Entity(
    tableName = "detachment_stratagems",
    primaryKeys = ["detachmentId", "stratagemId"],
    foreignKeys = [ForeignKey(
        entity = DetachmentEntity::class,
        parentColumns = ["id"], childColumns = ["detachmentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("detachmentId")]
)
data class DetachmentStratagemEntity(
    val detachmentId: String,
    val stratagemId: String,
    val name: String,
    val cpCost: Int,
    val phase: String,
    val whenText: String?,
    val targetText: String?,
    val effectText: String?,
    val textStatus: String,
    val sourcePage: Int
)

@Entity(
    tableName = "detachment_enhancements",
    primaryKeys = ["detachmentId", "enhancementId"],
    foreignKeys = [ForeignKey(
        entity = DetachmentEntity::class,
        parentColumns = ["id"], childColumns = ["detachmentId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("detachmentId")]
)
data class DetachmentEnhancementEntity(
    val detachmentId: String,
    val enhancementId: String,
    val name: String,
    val points: Int,
    val effectText: String?,
    val eligibilityText: String?,
    val textStatus: String
)

@Entity(
    tableName = "roster_detachments",
    primaryKeys = ["rosterId", "detachmentId"],
    foreignKeys = [
        ForeignKey(
            entity = RosterEntity::class,
            parentColumns = ["id"], childColumns = ["rosterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DetachmentEntity::class,
            parentColumns = ["id"], childColumns = ["detachmentId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("rosterId"), Index("detachmentId")]
)
data class RosterDetachmentEntity(
    val rosterId: String,
    val detachmentId: String
)
