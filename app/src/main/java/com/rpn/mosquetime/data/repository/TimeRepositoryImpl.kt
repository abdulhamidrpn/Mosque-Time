package com.rpn.mosquetime.data.repository

import android.os.Build
import android.util.Log
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.rpn.mosquetime.domain.time.CompatDayOfWeek
import com.rpn.mosquetime.domain.model.time.CompatLocalDateTime
import com.rpn.mosquetime.domain.model.time.CompatLocalTime
import com.rpn.mosquetime.domain.model.time.PrayerTime
import com.rpn.mosquetime.domain.model.time.NotificationTrigger
import com.rpn.mosquetime.domain.model.time.TimeState
import com.rpn.mosquetime.domain.time.TriggerType
import com.rpn.mosquetime.domain.time.minutesBetween
import com.rpn.mosquetime.domain.repository.TimeRepository
import io.ktor.util.toUpperCasePreservingASCIIRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.util.Calendar
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

import kotlinx.datetime.DayOfWeek as KtDayOfWeek

// Implementation of the TimeRepository.
class TimeRepositoryImpl() : TimeRepository {

    private var prayerList: List<PrayerTime> = listOf(
        PrayerTime("fajar", 5, 30),
        PrayerTime("dhuhr", 13, 30),
        PrayerTime("asr", 17, 0),
        PrayerTime("maghrib", 18, 30),
        PrayerTime("isha", 20, 50),
        PrayerTime("jumah", 13, 30),
        PrayerTime("sunrise", 21, 30),
    )

    override fun initialize(timings: List<PrayerTime>) {
        if (timings.isNotEmpty())
            prayerList = timings
    }

    override val timeFlow: Flow<TimeState> = flow {
        while (true) {
            val now = getCurrentCompatLocalDateTimeOptimized()
            val currentDateTime = now
            val currentTime = now.toLocalTime()
            val dayOfWeek = currentDateTime.dayOfWeek
            val isFriday = dayOfWeek == CompatDayOfWeek.FRIDAY

            // Filter active prayers: include "jumah" only on Fridays, "dhuhr" otherwise.
            val activePrayerTimes = prayerList.filter { prayer ->
                when (prayer.name.lowercase()) {
                    "jumah" -> isFriday
                    "dhuhr" -> !isFriday
                    else -> true
                }
            }.associate { it.name to CompatLocalTime(it.hour, it.minute, 0) }

            val notifications = mutableListOf<NotificationTrigger>()

            for ((name, pTime) in activePrayerTimes) {
                val prayer = prayerList.first { it.name == name }
                val currentTimeStr = currentTime.toString()
                val dayName = dayOfWeek.name.capitalize(Locale.ROOT)
                if (currentTime.isAfter(pTime)) {
                    // Prayer has passed.
                    val minutesPassed = minutesBetween(pTime, currentTime)
                    if (minutesPassed in listOf(5L, 7L, 9L)) {
                        notifications.add(
                            NotificationTrigger(
                                prayerTime = prayer,
                                triggerType = TriggerType.AFTER_PRAYER,
                                offsetMinutes = minutesPassed.toInt(),
                                triggerTime = currentTime,
                                message = "${name.toUpperCasePreservingASCIIRules()} prayer has passed by $minutesPassed minutes"
                            )
                        )
                    }
                } else if (currentTime.isBefore(pTime)) {
                    // Prayer is approaching.
                    val minutesTo = minutesBetween(currentTime, pTime)
                    if (minutesTo in listOf(1L, 3L, 5L, 30L)) {
                        notifications.add(
                            NotificationTrigger(
                                prayerTime = prayer,
                                triggerType = TriggerType.BEFORE_PRAYER,
                                offsetMinutes = -minutesTo.toInt(),
                                triggerTime = currentTime,
                                message = "${name.toUpperCasePreservingASCIIRules()} prayer will start in $minutesTo minutes"
                            )
                        )
                    }
                }
                // Exact match (prayer time now) is not handled as per requirements, but could be added if needed.
            }

            emit(TimeState(currentDateTime, notifications))

            // Delay for approximately 1 second. In practice, this provides precise enough ticking.
            // For more precision, consider aligning to system clock seconds, but this suffices for the requirements.
            delay(1000)
        }
    }.flowOn(Dispatchers.Default) // Run computations on a background dispatcher for efficiency.


    override fun getCurrentDate(): CompatLocalDateTime {
        return getCurrentCompatLocalDateTimeOptimized()
    }

    override fun getCurrentHijriDate(): CompatLocalDateTime {
        return getCurrentHijriCompatLocalDateTimeLegacy()
    }
}

@OptIn(ExperimentalTime::class)
private fun getCurrentCompatLocalDateTimeOptimized(): CompatLocalDateTime {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        getCurrentCompatLocalDateTimeModern()
    } else {
        getCurrentCompatLocalDateTimeLegacy()
    }
}

@OptIn(ExperimentalTime::class)
private fun getCurrentCompatLocalDateTimeModern(): CompatLocalDateTime {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val dayOfWeek = when (now.dayOfWeek) {
        KtDayOfWeek.MONDAY -> CompatDayOfWeek.MONDAY
        KtDayOfWeek.TUESDAY -> CompatDayOfWeek.TUESDAY
        KtDayOfWeek.WEDNESDAY -> CompatDayOfWeek.WEDNESDAY
        KtDayOfWeek.THURSDAY -> CompatDayOfWeek.THURSDAY
        KtDayOfWeek.FRIDAY -> CompatDayOfWeek.FRIDAY
        KtDayOfWeek.SATURDAY -> CompatDayOfWeek.SATURDAY
        KtDayOfWeek.SUNDAY -> CompatDayOfWeek.SUNDAY
    }
    return CompatLocalDateTime(
        now.year,
        now.month.ordinal,
        now.day,
        now.hour,
        now.minute,
        now.second,
        dayOfWeek
    )
}

private fun getCurrentCompatLocalDateTimeLegacy(): CompatLocalDateTime {
    val calendar = Calendar.getInstance()
    val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> CompatDayOfWeek.MONDAY
        Calendar.TUESDAY -> CompatDayOfWeek.TUESDAY
        Calendar.WEDNESDAY -> CompatDayOfWeek.WEDNESDAY
        Calendar.THURSDAY -> CompatDayOfWeek.THURSDAY
        Calendar.FRIDAY -> CompatDayOfWeek.FRIDAY
        Calendar.SATURDAY -> CompatDayOfWeek.SATURDAY
        Calendar.SUNDAY -> CompatDayOfWeek.SUNDAY
        else -> throw IllegalStateException("Invalid day of week")
    }
    return CompatLocalDateTime(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        calendar.get(Calendar.SECOND),
        dayOfWeek
    )
}

private fun getCurrentHijriCompatLocalDateTimeLegacy(): CompatLocalDateTime {
    val calendar = UmmalquraCalendar()
    val dayOfWeek = when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> CompatDayOfWeek.MONDAY
        Calendar.TUESDAY -> CompatDayOfWeek.TUESDAY
        Calendar.WEDNESDAY -> CompatDayOfWeek.WEDNESDAY
        Calendar.THURSDAY -> CompatDayOfWeek.THURSDAY
        Calendar.FRIDAY -> CompatDayOfWeek.FRIDAY
        Calendar.SATURDAY -> CompatDayOfWeek.SATURDAY
        Calendar.SUNDAY -> CompatDayOfWeek.SUNDAY
        else -> throw IllegalStateException("Invalid day of week")
    }
    return CompatLocalDateTime(
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH) + 1,
        calendar.get(Calendar.DAY_OF_MONTH),
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        calendar.get(Calendar.SECOND),
        dayOfWeek
    )
}