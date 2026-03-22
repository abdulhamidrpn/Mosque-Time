package com.rpn.salatetime.presentation.screen.main

import com.rpn.salatetime.domain.model.MosqueCompositeData
import com.rpn.salatetime.domain.model.NotificationTrigger
import com.rpn.salatetime.domain.model.PrayerTime
import com.rpn.salatetime.domain.model.TimeState

data class MainScreenState(
    // ── Auth ─────────────────────────────────────────────────────────────────
    //
    // isAuthReady: false until the first emission of settingsFlow is received.
    //   The NavGraph shows a SplashScreen while this is false so the user never
    //   sees a flash of the Login screen before being redirected to Home.
    //
    // userId: null  → not logged in  → Login destination
    //         blank → same as null
    //         value → logged in      → Home destination
    val isAuthReady: Boolean = false,
    val userId: String? = null,

    // ── Mosque data ───────────────────────────────────────────────────────────
    val isLoading: Boolean = false,
    val error: String? = null,
    val data: MosqueCompositeData? = null,
    val todayPrayerTime: PrayerTime? = null,

    // ── Display preferences (from DataStore) ──────────────────────────────────
    val is24HourFormat: Boolean = true,
    val showAmPm: Boolean = false,
    val showMosqueName: Boolean = false,
    val mosqueName: String = "",
    val mosqueMessage: String = "",
    val selectedBackgroundImage: String = "",

    // ── Notification banner ───────────────────────────────────────────────────
    val showNotificationBanner: Boolean = false,
    val showMessageNotification: Boolean = false,
    val activeNotifications: List<NotificationTrigger> = emptyList(),
) {
    /** Convenience: true when the user has a valid, non-blank userId. */
    val isLoggedIn: Boolean get() = !userId.isNullOrBlank()
}
data class MainScreenStatex(

    val isLoading: Boolean = true,
    val data: MosqueCompositeData? = null,
    val todayPrayerTime: PrayerTime? = null,
    val timeState: TimeState? = null,
    val activeNotifications: List<NotificationTrigger>? = null,
    val userId: String? = null,
    val error: String? = null,

    val backgroundBlur: Boolean = false,
    val showMessageNotification: Boolean = false,
    val showNotificationBanner: Boolean = false,
    val selectedBackgroundImage: String = "https://picsum.photos/1080/720",
    val is24HourFormat: Boolean = false,
    val showMosqueName: Boolean = false,
    val showAmPM: Boolean = true,
    val mosqueName: String = "",
    val mosqueMessage:String = "Welcome to the Masjid - This is a sample scrolling message to demonstrate marquee",

    val errorMessage: String? = null,
    val isOfflineMode: Boolean = false,
)