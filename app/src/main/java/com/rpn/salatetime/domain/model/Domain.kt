package com.rpn.salatetime.domain.model

// 1. Mosque Domain Model
data class Mosque(
    val id: String,
    val name: String?,
    val jumuaTime: String?,
    val bottomMessage: String?,
    val thumbnail: String?,
    val isActive: Boolean,
    val address: String?
)

// 2. Prayer Time Domain Model
data class PrayerTime(
    val date: String,
    val fajr: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val sunrise: String,
    val jumah: String = "",
){

    fun toCompatLocalTimeList(): List<CompatLocalTime> = listOfNotNull(
        fajr.toCompatTime("fajr"),
        sunrise.toCompatTime("sunrise"),
        dhuhr.toCompatTime("dhuhr"),
        asr.toCompatTime("asr"),
        maghrib.toCompatTime("maghrib"),
        isha.toCompatTime("isha"),
        jumah.toCompatTime("jumah"),
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

// 3. Mosque Slide Domain Model
data class MosqueSlide(
    val id: String,
    val imageUrl: String,
    val displayOrder: Int,
    val isDownloaded: Boolean // UI logic: check if localPath is present
)