package com.rpn.salatetime.domain.model

data class CompatLocalDateTime(
    val year: Int,
    val month: Int,        // 1–12
    val dayOfMonth: Int,
    val hour: Int,
    val minute: Int,
    val second: Int,
    val dayOfWeek: CompatDayOfWeek,
) {

    fun countDownSeconds(): Int = 60 - second
    fun toLocalTime(): CompatLocalTime =
        CompatLocalTime(hour = hour, minute = minute, second = second)

    fun toFormattedDate(): String = "$dayOfMonth ${month.getMonth()} $year"

    fun toFormattedDateDatabase(): String =
        "$year-${month.toString().padStart(2, '0')}-${dayOfMonth.toString().padStart(2, '0')}"

    fun toFormattedHijriDate(): String = "$dayOfMonth ${month.getHijriMonth()} $year"
}

/*Track Wakt time and compare with prayer time*/
data class  CompatLocalTime(
    val name: String = "",
    val hour: Int,
    val minute: Int,
    val second: Int = 0,
) {
    fun isAfter(other: CompatLocalTime): Boolean {
        if (hour != other.hour) return hour > other.hour
        if (minute != other.minute) return minute > other.minute
        return second > other.second
    }

    fun isBefore(other: CompatLocalTime): Boolean = !isAfter(other) && this != other

    fun toTotalMinutes(): Long = hour * 60L + minute
    fun toTotalSeconds(): Long = hour * 3600L + minute * 60L + second
    fun toMillis(): Long = (hour * 3600L + minute * 60L + second) * 1_000L
    fun countDown(): Int = 60 - second

    /**
     * Returns a 12-hour adjusted copy.
     * AM/PM suffix is rendered in [toDisplayString] — not stored in the value.
     */
    private fun to12Hour(): CompatLocalTime {
        val h = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        return copy(hour = h)
    }

    /** "HH:mm" 24-hour — used for DB keys and time comparisons. */
    override fun toString(): String =
        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

    /** Human-readable display: "14:30" or "02:30 PM". */
    fun toDisplayString(is24: Boolean = true, showSuffix: Boolean = false): String {
        if (is24) return toString()
        val t = to12Hour()
        val suffix = if (showSuffix) {
            if (hour < 12) "AM" else "PM"
        } else ""
        return "${t.hour.toString().padStart(2, '0')}:${
            t.minute.toString().padStart(2, '0')
        } $suffix"
    }
}

data class NotificationTrigger(
    val compatLocalTime: CompatLocalTime,
    val triggerType: TriggerType,
    val offsetMinutes: Int,     // positive = after prayer, negative = before
    val triggerTime: CompatLocalTime,
    val imageMessage: String? = null, // Optional single image URL or resource (backward compatibility)
    val message: String = "",
)

data class TimeState(
    val currentDateTime: CompatLocalDateTime,
    val currentNotification: NotificationTrigger?,
    val hijriDateFormatted: String = "",
    val prayerTime: PrayerTime? = null
)