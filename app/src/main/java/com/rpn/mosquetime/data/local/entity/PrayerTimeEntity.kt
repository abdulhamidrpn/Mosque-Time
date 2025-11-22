package com.rpn.mosquetime.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.rpn.mosquetime.domain.manager.SyncStatus



// Updated Entity for Prayer Times (aligned with MosqueTime structure)
@Entity(
    tableName = "prayer_times",
    foreignKeys = [ForeignKey(
        entity = MosqueInfoEntity::class,
        parentColumns = ["documentId"],
        childColumns = ["mosqueId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PrayerTimeEntity(
    @PrimaryKey val id: String,
    val mosqueId: String,
    val date: String, // readable date
    val hijriDate: String? = null,
    val hijriMonth: String? = null,
    val hijriYear: String? = null,
    val weekdayAr: String? = null,
    val weekdayEn: String? = null,
    val timestamp: String? = null,
    val fajr: String = "00:00",
    val sunrise: String = "00:00",
    val dhuhr: String = "00:00",
    val asr: String = "00:00",
    val maghrib: String = "00:00",
    val isha: String = "00:00",
    val sunset: String? = null,
    val imsak: String? = null,
    val lastModified: Long = System.currentTimeMillis(),
    val syncStatus: SyncStatus = SyncStatus.SYNCED
)