package com.rpn.mosquetime.model

import com.google.gson.Gson
import com.rpn.mosquetime.utils.GeneralUtils
import org.koin.core.component.KoinComponent
import java.io.Serializable

data class TimingDetails(
    var asr: String? = null,
    var isha: String? = null,
    var imsak: String? = null,
    var dhuhr: String? = null,
    var fajr: String? = null,
    var maghrib: String? = null
) : Serializable, KoinComponent {
    override fun toString(): String {
        return Gson().toJson(this)
    }


    fun to24() {
        fajr = fajr?.let { GeneralUtils.convertTimeTo24(it) }
        dhuhr = dhuhr?.let { GeneralUtils.convertTimeTo24(it) }
        asr = asr?.let { GeneralUtils.convertTimeTo24(it) }
        maghrib = maghrib?.let { GeneralUtils.convertTimeTo24(it) }
        isha = isha?.let { GeneralUtils.convertTimeTo24(it) }
    }

    fun toAM() {
        fajr = fajr?.let { GeneralUtils.convertTimeToAm(it) }
        dhuhr = dhuhr?.let { GeneralUtils.convertTimeToAm(it) }
        asr = asr?.let { GeneralUtils.convertTimeToAm(it) }
        maghrib = maghrib?.let { GeneralUtils.convertTimeToAm(it) }
        isha = isha?.let { GeneralUtils.convertTimeToAm(it) }
    }
}