package com.rpn.mosquetime.domain.repository


import com.rpn.mosquetime.domain.model.time.CompatLocalDateTime
import com.rpn.mosquetime.domain.model.time.PrayerTime
import com.rpn.mosquetime.domain.model.time.TimeState
import kotlinx.coroutines.flow.Flow

interface TimeRepository {
    val timeFlow: Flow<TimeState>

    fun initialize(timings: List<PrayerTime>)

    fun getCurrentDate(): CompatLocalDateTime

    fun getCurrentHijriDate(): CompatLocalDateTime
}