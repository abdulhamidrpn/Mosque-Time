package com.rpn.salatetime.data.local.db

import com.rpn.salatetime.domain.model.Mosque
import com.rpn.salatetime.domain.model.MosqueDto
import com.rpn.salatetime.domain.model.MosqueEntity
import com.rpn.salatetime.domain.model.MosqueSlide
import com.rpn.salatetime.domain.model.MosqueSlideDto
import com.rpn.salatetime.domain.model.MosqueSlideEntity
import com.rpn.salatetime.domain.model.PrayerTime
import com.rpn.salatetime.domain.model.PrayerTimeDto
import com.rpn.salatetime.domain.model.PrayerTimeEntity

fun MosqueDto.toEntity(): MosqueEntity = MosqueEntity(
    id = id,
    ownerUid = ownerUid,
    name = name,
    jumuaTime = jumuaTime,
    bottomMessage = bottomMessage,
    dataVersion = dataVersion,
    image = image,
    isActive = isActive,
    latLon = latLon,
    address = address
    // lastSyncTimestamp uses default value
)

fun PrayerTimeDto.toEntity(): PrayerTimeEntity = PrayerTimeEntity(
    mosqueId = mosqueId,
    date = date,
    fajr = fajr,
    dhuhr = dhuhr,
    asr = asr,
    maghrib = maghrib,
    isha = isha,
    sunrise = sunrise
)

fun MosqueSlideDto.toEntity(): MosqueSlideEntity = MosqueSlideEntity(
    id = id,
    mosqueId = mosqueId,
    imageUrl = imageUrl,
    displayOrder = displayOrder,
    localPath = null // Will be updated by a Download Worker later
)








// --- Mosque Mappers ---
fun MosqueEntity.toDomain(): Mosque = Mosque(
    id = id,
    name = name,
    jumuaTime = jumuaTime,
    bottomMessage = bottomMessage,
    thumbnail = localPath,
    isActive = isActive,
    address = address
)

// --- Prayer Time Mappers ---
fun PrayerTimeEntity.toDomain(): PrayerTime = PrayerTime(
    date = date,
    fajr = fajr,
    dhuhr = dhuhr,
    asr = asr,
    maghrib = maghrib,
    isha = isha,
    sunrise = sunrise
)

// --- Mosque Slide Mappers ---
fun MosqueSlideEntity.toDomain(): MosqueSlide = MosqueSlide(
    id = id,
    imageUrl = localPath ?: imageUrl,
    displayOrder = displayOrder,
    isDownloaded = localPath != null
)