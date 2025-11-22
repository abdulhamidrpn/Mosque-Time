package com.rpn.mosquetime.domain.model

data class Timings(
    val fajr: String = "00:00",
    val dhuhr: String = "00:00",
    val asr: String = "00:00",
    val maghrib: String = "00:00",
    val isha: String = "00:00",
    val sunrise: String = "00:00"
)
