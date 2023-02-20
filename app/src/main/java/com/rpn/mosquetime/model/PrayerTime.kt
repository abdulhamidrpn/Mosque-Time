package com.rpn.mosquetime.model

import com.google.gson.Gson

data class PrayerTime(
    val date: List<MosqueTime>? = null
){
    override fun toString(): String {
        return Gson().toJson(this)
    }
}