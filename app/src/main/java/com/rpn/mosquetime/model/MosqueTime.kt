package com.rpn.mosquetime.model

import com.google.gson.Gson
import java.io.Serializable

class MosqueTime(
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