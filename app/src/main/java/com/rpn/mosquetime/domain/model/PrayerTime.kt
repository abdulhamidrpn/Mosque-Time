package com.rpn.mosquetime.domain.model

data class PrayerTime(
    val month: Int,
    val date: Int,
    val timings: Timings,
)