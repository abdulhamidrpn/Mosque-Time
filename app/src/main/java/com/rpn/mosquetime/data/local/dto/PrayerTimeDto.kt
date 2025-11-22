package com.rpn.mosquetime.data.local.dto

import com.google.gson.Gson
import java.io.Serializable

data class PrayerTimeDto(
    val date: List<MosqueTimeDto>? = null
)

data class MosqueTimeDto(

    var readable: String? = null,
    var hijri: Hijri? = null,
    var timingDetails: TimingDetails? = null,
    var timestamp: String? = null,
    var sunrise: String? = null,
    var sunset: String? = null
): Serializable {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}


data class TimingDetails(
    var asr: String? = null,
    var isha: String? = null,
    var imsak: String? = null,
    var dhuhr: String? = null,
    var fajr: String? = null,
    var maghrib: String? = null
): Serializable {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}

