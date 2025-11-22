package com.rpn.mosquetime.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.rpn.mosquetime.domain.manager.SyncStatus

// Entity for Messages
@Entity(
    tableName = "mosque_messages",
    foreignKeys = [ForeignKey(
        entity = MosqueInfoEntity::class,
        parentColumns = ["documentId"],
        childColumns = ["mosqueId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MosqueMessageEntity(
    @PrimaryKey val id: String,
    val mosqueId: String,
    val messageKey: String,
    val messageUrl: String,
    val localImagePath: String? = null,
    val orderIndex: Int,
    val lastModified: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)