package com.rpn.mosquetime.domain.model.time

import android.os.Parcelable
import com.rpn.mosquetime.domain.time.CompatDayOfWeek
import com.rpn.mosquetime.domain.time.TriggerType
import com.rpn.mosquetime.domain.time.getHijriMonth
import com.rpn.mosquetime.domain.time.getMonth
import kotlinx.parcelize.Parcelize


// Custom LocalDateTime class for compatibility across API levels.
data class CompatLocalDateTime(
    val year: Int,
    val month: Int, // 1-12
    val dayOfMonth: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val dayOfWeek: CompatDayOfWeek
) {
    fun toLocalTime(): CompatLocalTime = CompatLocalTime(hour, minute, second)
    fun toOldLocalTime(): CompatLocalTime = CompatLocalTime(hour, minute, second)
    fun toFormattedDate(): String = "$dayOfMonth ${month.getMonth()} $year"
    fun toFormattedHijriDate(): String = "$dayOfMonth ${month.getHijriMonth()} $year"

}


// Custom LocalTime class for compatibility.
@Parcelize
data class CompatLocalTime(
    val hour: Int,
    val minute: Int,
    val second: Int = 0
) : Parcelable{

    fun countDown(): Int = 60 - second

    fun toMinutes(): Int = hour * 60 + minute

    fun toMillis(): Int = hour * 60 + minute * 60 + second * 1000


    fun isAfter(other: CompatLocalTime): Boolean {
        if (hour > other.hour) return true
        if (hour < other.hour) return false
        if (minute > other.minute) return true
        if (minute < other.minute) return false
        return second > other.second
    }

    fun isBefore(other: CompatLocalTime): Boolean {
        return !isAfter(other) && this != other
    }

    fun toTotalSeconds(): Long = hour * 3600L + minute * 60L + second

    fun toTotalMinute(): Long = hour * 60L + minute

    fun timeFormat(is24: Boolean = true): CompatLocalTime {
        if (!is24) {
            val h = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            return CompatLocalTime(h, minute, second)
        }
        return this
    }

    override fun toString(): String {
        return "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
    }
}

data class NotificationTrigger(
    val prayerTime: PrayerTime,
    val triggerType: TriggerType,
    val offsetMinutes: Int, // Positive for after, negative for before
    val triggerTime: CompatLocalTime,
    val message: String = ""
)

data class TimeStatus(
    val currentTime: CompatLocalTime,
    val activeTriggers: PrayerNotificationTrigger,
    val formattedMessage: String = "",
)

// Data class for the emitted state.
data class TimeState(
    val currentDateTime: CompatLocalDateTime,
    val notifications: List<NotificationTrigger>
)

data class PrayerNotificationTrigger(
    val prayerTime: PrayerTime,
    val triggerType: TriggerType,
    val offsetMinutes: Int, // Positive for after, negative for before
    val triggerTime: CompatLocalTime,
    val message: String = ""
)


@Parcelize
data class PrayerTime(
    val name: String,
    val hour: Int,
    val minute: Int
) : Parcelable {

    fun toTimeString(): String =
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    fun toMinutes(): Int = hour * 60 + minute
    fun toLocalTime(): CompatLocalTime = CompatLocalTime(hour, minute)
}