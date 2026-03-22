package com.rpn.salatetime.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

// --- Local Entities (Room) ---
@Entity(tableName = "mosques")
data class MosqueEntity(
    @PrimaryKey val id: String,
    val ownerUid: String,
    val name: String,
    val jumuaTime: String?,
    val bottomMessage: String?,
    val dataVersion: Long,
    val image: String?,
    val localPath: String? = null, // For offline image caching
    val isActive: Boolean,
    val latLon: String?,
    val address: String?,
    val lastSyncTimestamp: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "prayer_times",
    primaryKeys = ["mosqueId", "date"],
    foreignKeys = [ForeignKey(
        entity = MosqueEntity::class,
        parentColumns = ["id"],
        childColumns = ["mosqueId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PrayerTimeEntity(
    val mosqueId: String,
    val date: String,
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val sunrise: String
) {

    fun toCompatLocalTimeList(
        jumuaTime: String? = null,
    ): List<CompatLocalTime> = listOfNotNull(
        fajr.toCompatTime("fajr"),
        sunrise.toCompatTime("sunrise"),
        dhuhr.toCompatTime("dhuhr"),
        asr.toCompatTime("asr"),
        maghrib.toCompatTime("maghrib"),
        isha.toCompatTime("isha"),
        jumuaTime?.takeIf { it.isNotBlank() }?.toCompatTime("jumah"),
    )

    /**
     * Parses "HH:mm" or "HH:mm:ss" into a [CompatLocalTime].
     * Returns midnight (00:00) on any parse failure — never throws.
     */
    private fun String.toCompatTime(name: String): CompatLocalTime =
        runCatching {
            val parts = trim().split(":")
            CompatLocalTime(name = name, hour = parts[0].toInt(), minute = parts[1].toInt())
        }.getOrDefault(CompatLocalTime(name = name, hour = 0, minute = 0))
}

@Entity(
    tableName = "mosque_slides",
    foreignKeys = [ForeignKey(
        entity = MosqueEntity::class,
        parentColumns = ["id"],
        childColumns = ["mosqueId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class MosqueSlideEntity(
    @PrimaryKey val id: String,
    val mosqueId: String,
    val imageUrl: String,
    val localPath: String? = null, // For offline image caching
    val displayOrder: Int
)

// --- Aggregated Result for UI ---
data class MosqueCompositeData(
    val mosque: MosqueEntity? = null,
    val todayPrayerTime: PrayerTimeEntity? = null, // Usually filtered for current month/day in UI
    val slides: List<MosqueSlideEntity> = emptyList()
)
