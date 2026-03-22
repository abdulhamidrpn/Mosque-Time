package com.rpn.salatetime.data.repository

import android.os.Build
import com.github.msarhan.ummalqura.calendar.UmmalquraCalendar
import com.rpn.salatetime.data.local.datastore.SettingsRepository
import com.rpn.salatetime.domain.model.CompatDayOfWeek
import com.rpn.salatetime.domain.model.CompatLocalDateTime
import com.rpn.salatetime.domain.model.CompatLocalTime
import com.rpn.salatetime.domain.model.NotificationTrigger
import com.rpn.salatetime.domain.model.TimeState
import com.rpn.salatetime.domain.model.TriggerType
import com.rpn.salatetime.domain.model.minutesBetween
import com.rpn.salatetime.domain.repository.TimeRepository
import com.rpn.salatetime.utils.Constants
import com.rpn.salatetime.utils.LocalizationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import timber.log.Timber
import java.util.Calendar
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlinx.datetime.DayOfWeek as KtDayOfWeek
/*This is my new time repostiory i'm updating from my old project old one is properly sending notification but this one is not returning any notification. with respect to prayer time. */

class TimeRepositoryImpl(
    private val settingsRepository: SettingsRepository,
) : TimeRepository {
    @Volatile
    private var prayerList: List<CompatLocalTime> = listOf(
        CompatLocalTime("fajr", 5, 30),
        CompatLocalTime("sunrise", 6, 45),
        CompatLocalTime("dhuhr", 13, 30),
        CompatLocalTime("asr", 17, 0),
        CompatLocalTime("maghrib", 18, 30),
        CompatLocalTime("isha", 20, 50),
        CompatLocalTime("jumah", 13, 30),
    )

    override fun initialize(timings: List<CompatLocalTime>) {
        if (timings.isNotEmpty()) prayerList = timings
        Timber.d("Notification Prayer List $prayerList")
    }

    override val timeFlow: Flow<TimeState> = channelFlow {

        // Keep language cached — avoids hitting DataStore on every tick
        var language = "en"
        launch {
            runCatching {
                settingsRepository.settingsFlow.collect { language = it.language }
            }.onFailure { Timber.e(it, "Failed to collect settings") }
        }

        var lastDay = -1
        var hijriDate = ""

        while (true) {
            // Align to the next wall-clock second so the display never drifts
            delay(1_000L - System.currentTimeMillis() % 1_000L)

            val now = now()

            // Detect new day
            if (now.dayOfMonth != lastDay) {
                if (lastDay != -1) {
                    Timber.i("🌅 New day started: ${now.toFormattedDateDatabase()} (${now.dayOfWeek})")
                }
                lastDay = now.dayOfMonth
                hijriDate = hijriNow().toFormattedHijriDate()
            }

            val isFriday = now.dayOfWeek == CompatDayOfWeek.FRIDAY
            val currentTime = now.toLocalTime()

            val currentNotification = prayerList
                .filter { it.isActiveToday(isFriday) }
                .mapNotNull { prayerTime ->
                    prayerTime.buildTrigger(currentTime, prayerTime, language)
                }
                .minByOrNull { Math.abs(it.offsetMinutes) } // Select the most urgent notification (closest to prayer time)

//            Timber.i("\n now : $now \n currentNotification : $currentNotification \n hijriDate : $hijriDate \n prayerList : $prayerList")
            send(TimeState(now, currentNotification, hijriDate))
        }

    }.flowOn(Dispatchers.Default)

    override fun getCurrentDate(): CompatLocalDateTime = now()
    override fun getCurrentHijriDate(): CompatLocalDateTime = hijriNow()
}

// ─── Prayer helpers ───────────────────────────────────────────────────────────

private fun CompatLocalTime.isActiveToday(isFriday: Boolean): Boolean = when (name.lowercase()) {
    "jumah" -> isFriday
    "dhuhr" -> !isFriday
    else -> true
}

private fun CompatLocalTime.buildTrigger(
    currentTime: CompatLocalTime,
    prayerTime: CompatLocalTime,
    language: String,
): NotificationTrigger? {
    return when {
        currentTime.isAfter(prayerTime) -> {
            val minutes = minutesBetween(prayerTime, currentTime)
            //Timber.d("After Prayer Minutes between : $minutes \ncurrentTime: $currentTime \nprayerTime: $prayerTime")
            if (minutes !in Constants.afterPrayerOffsets) return null
            NotificationTrigger(
                compatLocalTime = this,
                triggerType = TriggerType.AFTER_PRAYER,
                offsetMinutes = minutes.toInt(),
                triggerTime = currentTime,
                message = LocalizationHelper.getPrayerPassedMessage(name, minutes, language),
            )
        }

        currentTime.isBefore(prayerTime) -> {
            val minutes = minutesBetween(currentTime, prayerTime)
            //Timber.d("Before Prayer Minutes between : $minutes \ncurrentTime: $currentTime \nprayerTime: $prayerTime")

            if (minutes !in Constants.beforePrayerOffsets) return null
            NotificationTrigger(
                compatLocalTime = this,
                triggerType = TriggerType.BEFORE_PRAYER,
                offsetMinutes = -minutes.toInt(),
                triggerTime = currentTime,
                message = LocalizationHelper.getPrayerApproachingMessage(name, minutes, language),
            )
        }

        else -> null
    }
}

// ─── Time helpers ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalTime::class)
private fun now(): CompatLocalDateTime {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val t = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        return CompatLocalDateTime(
            year = t.year,
            month = t.month.number,
            dayOfMonth = t.day,
            hour = t.hour,
            minute = t.minute,
            second = t.second,
            dayOfWeek = t.dayOfWeek.toCompat()
        )
    }
    return Calendar.getInstance().toCompat()
}

private fun hijriNow(): CompatLocalDateTime = UmmalquraCalendar().toCompat()

private fun KtDayOfWeek.toCompat() =
    CompatDayOfWeek.entries[this.ordinal] // both enums are Mon–Sun ordered

private fun Calendar.toCompat() = CompatLocalDateTime(
    year = get(Calendar.YEAR),
    month = get(Calendar.MONTH) + 1,
    dayOfMonth = get(Calendar.DAY_OF_MONTH),
    hour = get(Calendar.HOUR_OF_DAY),
    minute = get(Calendar.MINUTE),
    second = get(Calendar.SECOND),
    dayOfWeek = get(Calendar.DAY_OF_WEEK).calendarDayToCompat(),
)

private fun Int.calendarDayToCompat() = when (this) {
    Calendar.MONDAY -> CompatDayOfWeek.MONDAY
    Calendar.TUESDAY -> CompatDayOfWeek.TUESDAY
    Calendar.WEDNESDAY -> CompatDayOfWeek.WEDNESDAY
    Calendar.THURSDAY -> CompatDayOfWeek.THURSDAY
    Calendar.FRIDAY -> CompatDayOfWeek.FRIDAY
    Calendar.SATURDAY -> CompatDayOfWeek.SATURDAY
    else -> CompatDayOfWeek.SUNDAY
}