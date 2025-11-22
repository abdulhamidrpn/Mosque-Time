package com.rpn.mosquetime.domain.model

data class MasjidInfo(
    val name: String? = null,
    val logoUrl: String? = null,
    val thumbnail: String? = null,
    val address: String? = null,
    val website: String? = null,
    val jumua: String = "00:00",
    val message: String? = null,
    val imageMessages: List<Message> = emptyList(),
    val prayerTimes: List<PrayerTime> = emptyList()
)