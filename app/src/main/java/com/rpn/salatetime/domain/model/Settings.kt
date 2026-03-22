package com.rpn.salatetime.domain.model

data class Settings(
    val userId: String = "",
    val userEmail: String = "",
    val mosqueId: String = "",

    val shouldShowMosqueName: Boolean = true,
    val mosqueName: String = "",
    val message: String = "",
    val is24HourFormat: Boolean = true,
    val fajrTime: String = "05:00",
    val dhuhrTime: String = "12:30",
    val asrTime: String = "15:45",
    val maghribTime: String = "18:20",
    val ishaTime: String = "20:00",
    val jumahTime: String = "12:45",
    val sunriseTime: String = "06:15",

    val backgroundImage: String = "", // could be file path or URL
    val prePrayerNotification: Boolean = true,
    val postPrayerNotification: Boolean = true,

    val locationLatitude: Double? = null,
    val locationLongitude: Double? = null,
    val calculationMethod: String = "MWL", // Muslim World League (default)
    val madhab: String = "Shafi", // or Hanafi

    val language: String = "en",
    val theme: Theme = Theme.SYSTEM
)

enum class Theme {
    LIGHT,    DARK, SYSTEM
}
enum class PrayerName {
    FAJR, SUNRISE, DHUHR, ASR, MAGHRIB, ISHA
}