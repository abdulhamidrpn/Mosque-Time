package com.rpn.mosquetime.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.rpn.mosquetime.domain.manager.SyncStatus


// Entity for Mosque Information
// Updated Entity for Mosque Information
@Entity(tableName = "mosque_info")
data class MosqueInfoEntity(
    @PrimaryKey val documentId: String,
    val masjidName: String,
    val ownerUid: String,
    val ownerName: String = "",
    val activated: Boolean,
    val city: String = "",
    val country: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val image: String? = null,
    val localImagePath: String? = null,
    val jumua: String = "00:00",
    val bottomMessage: String = "",
    val creationDate: Long = System.currentTimeMillis(),
    val lastModified: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)