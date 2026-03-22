package com.rpn.salatetime.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─────────────────────────────────────────────────────────────────────────────
// Serializable DTO for Supabase <-> app communication
// ─────────────────────────────────────────────────────────────────────────────

@Serializable
data class MosqueDto(
    @SerialName("id") val id: String,
    @SerialName("owner_uid") val ownerUid: String,
    @SerialName("name")val name: String = "",
    @SerialName("jumua_time") val jumuaTime: String? = null,
    @SerialName("bottom_message") val bottomMessage: String? = null,
    @SerialName("data_version") val dataVersion: Long = 1,
    @SerialName("image") val image: String? = null,
    @SerialName("lat_lon") val latLon: String? = null,
    @SerialName("address") val address: String? = null,
    @SerialName("is_active") val isActive: Boolean = false
)

@Serializable
data class PrayerTimeDto(
    @SerialName("mosque_id") val mosqueId: String,
    @SerialName("date") val date: String,
    @SerialName("sunrise") val sunrise: String,
    @SerialName("fajr") val fajr: String,
    @SerialName("dhuhr") val dhuhr: String,
    @SerialName("asr") val asr: String,
    @SerialName("maghrib") val maghrib: String,
    @SerialName("isha") val isha: String,
)


@Serializable
data class MosqueSlideDto(
    @SerialName("id") val id: String,
    @SerialName("mosque_id") val mosqueId: String,
    @SerialName("image_url") val imageUrl: String,
    @SerialName("display_order") val displayOrder: Int
)
