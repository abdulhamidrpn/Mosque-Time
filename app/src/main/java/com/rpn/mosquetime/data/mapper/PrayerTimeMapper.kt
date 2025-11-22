package com.rpn.mosquetime.data.mapper

import com.rpn.mosquetime.data.local.entity.PrayerTimeEntity
import com.rpn.mosquetime.domain.Mapper
import com.rpn.mosquetime.domain.model.PrayerTime

// Prayer Time Mapper
class PrayerTimeMapper : Mapper<List<PrayerTimeEntity>, List<PrayerTime>> {

    override fun mapToDomain(entity: List<PrayerTimeEntity>): List<com.rpn.mosquetime.domain.model.PrayerTime> {
        return entity.groupBy { extractMonthAndDateFromEntity(it) }
            .map { (monthDate, entities) ->
               PrayerTime(
                    month = monthDate.first,
                    date = monthDate.second,
                    timings = com.rpn.mosquetime.domain.model.Timings(
                        fajr = entities.first().fajr,
                        dhuhr = entities.first().dhuhr,
                        asr = entities.first().asr,
                        maghrib = entities.first().maghrib,
                        isha = entities.first().isha,
                        sunrise = entities.first().sunrise
                    )
                )
            }
    }

    override fun mapToEntity(domain: List<com.rpn.mosquetime.domain.model.PrayerTime>): List<PrayerTimeEntity> {
        return domain.map { prayerTime ->
            PrayerTimeEntity(
                id = "${prayerTime.month}_${prayerTime.date}",
                mosqueId = "", // Should be provided separately
                date = "${prayerTime.date}/${prayerTime.month}",
                fajr = prayerTime.timings.fajr,
                sunrise = prayerTime.timings.sunrise,
                dhuhr = prayerTime.timings.dhuhr,
                asr = prayerTime.timings.asr,
                maghrib = prayerTime.timings.maghrib,
                isha = prayerTime.timings.isha
            )
        }
    }

    private fun extractMonthAndDateFromEntity(entity: PrayerTimeEntity): Pair<Int, Int> {
        // Extract month and date from the date string or timestamp
        // This is a simplified implementation - adjust based on your date format
        return try {
            val parts = entity.date.split("/")
            if (parts.size >= 2) {
                Pair(parts[1].toInt(), parts[0].toInt())
            } else {
                Pair(1, 1)
            }
        } catch (e: Exception) {
            Pair(1, 1)
        }
    }
}
