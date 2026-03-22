package com.rpn.salatetime.domain.repository


import com.rpn.salatetime.domain.model.CompatLocalDateTime
import com.rpn.salatetime.domain.model.CompatLocalTime
import com.rpn.salatetime.domain.model.TimeState
import kotlinx.coroutines.flow.Flow

interface TimeRepository {
    val timeFlow: Flow<TimeState>

    fun initialize(timings: List<CompatLocalTime>)

    fun getCurrentDate(): CompatLocalDateTime

    fun getCurrentHijriDate(): CompatLocalDateTime
}