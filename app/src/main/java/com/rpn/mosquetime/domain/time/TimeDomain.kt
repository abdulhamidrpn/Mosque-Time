package com.rpn.mosquetime.domain.time

import com.rpn.mosquetime.domain.model.time.CompatLocalTime

fun Int.getMonth(): String {
    return when (this) {
        1 -> "Jan"
        2 -> "Feb"
        3 -> "Mar"
        4 -> "Apr"
        5 -> "May"
        6 -> "Jun"
        7 -> "Jul"
        8 -> "Aug"
        9 -> "Sep"
        10 -> "Oct"
        11 -> "Nov"
        12 -> "Dec"
        else -> ""

    }
}

fun Int.getHijriMonth(): String {
    return when (this) {
        1 -> "Muh"
        2 -> "Saf"
        3 -> "R-Aw"
        4 -> "R-Th"
        5 -> "J-Aw"
        6 -> "J-Th"
        7 -> "Raj"
        8 -> "Sha"
        9 -> "Ram"
        10 -> "Sha"
        11 -> "Qad"
        12 -> "Hij"
        else -> ""

    }
}


// Function to calculate minutes between two times (end - start) / 60.
fun minutesBetween(start: CompatLocalTime, end: CompatLocalTime): Long {
    return (end.toTotalMinute() - start.toTotalMinute())
}


enum class TriggerType {
    BEFORE_PRAYER,
    AFTER_PRAYER,
    SPECIAL_BEFORE // For Asr 30-minute notification
}


// Custom DayOfWeek enum to avoid java.time dependency.
enum class CompatDayOfWeek {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY
}