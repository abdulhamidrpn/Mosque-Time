package com.rpn.mosquetime.presentation.screen.main

import com.rpn.mosquetime.domain.model.time.CompatLocalTime
import com.rpn.mosquetime.domain.model.time.PrayerTime
import com.rpn.mosquetime.domain.time.TriggerType
import com.rpn.mosquetime.domain.model.MasjidInfo
import com.rpn.mosquetime.domain.model.Timings
import kotlin.time.Duration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
data class NextPrayer(
    val label: String = "",
    val at: CompatLocalTime? = null, // can be null if no more prayers today
    val remaining: Duration? = null         // can be negative if passed
)

data class MainScreenState(
    val nextPrayer: NextPrayer? = null,
    val isLoading: Boolean = false,
    val weekday: String = "",
    val enDate: String = "",
    val hijriDate: String = "",
    val timings: Timings = Timings(),
    val jummaTiming: String = "00:00",
    val masjidInfo: MasjidInfo = MasjidInfo(),
    val backgroundBlur: Boolean = false,
    val error: String? = null,
    val notifications: List<MainNotification> = emptyList(),
    val currentNotification: MainNotification = MainNotification(),
    val showNotificationBanner: Boolean = false,
    val selectedBackgroundImage: String = "",
    val is24HourFormat: Boolean = true,
    val showMosqueName: Boolean = false,
    val mosqueName: String = "",
    val mosqueMessage:String = "",
    val now: CompatLocalTime = CompatLocalTime(0, 0, 0),
    val isNavigationTriggered: Boolean = false,
)

@Parcelize
data class MainNotification(
    val id: String = "", // Unique ID
    val title: String = "",
    val message: String = "",
    val imageMessage: String? = null, // Optional image URL or resource
    val timestamp: Long = 0L, // Epoch time in millis
    val type: NotificationType = NotificationType.PRAYER_TIME,
    val prayerTime: PrayerTime? = null, // Relevant for prayer time notifications
    val triggerType: TriggerType = TriggerType.AFTER_PRAYER, // Default to at time
    val offsetMinutes: Int = 0, // Positive for after, negative for before
    val triggerTime: CompatLocalTime? = null, // Calculated trigger time (if applicable)
): Parcelable

enum class NotificationType {
    PRAYER_TIME,
    MESSAGE,
    SYSTEM
}
