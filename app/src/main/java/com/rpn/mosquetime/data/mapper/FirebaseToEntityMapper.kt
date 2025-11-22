package com.rpn.mosquetime.data.mapper

import com.rpn.mosquetime.data.local.dto.MosqueInfoDto
import com.rpn.mosquetime.data.local.dto.MosqueTimeDto
import com.rpn.mosquetime.data.local.entity.MosqueInfoEntity
import com.rpn.mosquetime.data.local.entity.MosqueMessageEntity
import com.rpn.mosquetime.data.local.entity.PrayerTimeEntity
import com.rpn.mosquetime.domain.manager.SyncStatus


// Firebase to Entity Mapper for MasjidInfo
class FirebaseToEntityMapper {

    fun mapMasjidInfoToEntity(
        firebaseModel: MosqueInfoDto,
        localImagePath: String? = null
    ): MosqueInfoEntity {
        return MosqueInfoEntity(
            documentId = firebaseModel.documentId,
            masjidName = firebaseModel.masjidName,
            ownerUid = firebaseModel.ownerUid,
            ownerName = firebaseModel.ownerName,
            activated = firebaseModel.activated,
            city = firebaseModel.city,
            country = firebaseModel.country,
            email = firebaseModel.email,
            phoneNumber = firebaseModel.phoneNumber,
            latitude = firebaseModel.latitude,
            longitude = firebaseModel.longitude,
            image = firebaseModel.image,
            localImagePath = localImagePath,
            jumua = firebaseModel.jumua,
            bottomMessage = firebaseModel.bottomMessage,
            creationDate = firebaseModel.creationDate?.time ?: System.currentTimeMillis(),
            lastModified = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
    }

    fun mapMosqueTimeToEntity(
        mosqueTimeDto: MosqueTimeDto,
        mosqueId: String,
        dateKey: String
    ): PrayerTimeEntity {
        return PrayerTimeEntity(
            id = "${mosqueId}_$dateKey",
            mosqueId = mosqueId,
            date = mosqueTimeDto.readable ?: dateKey,
            hijriDate = mosqueTimeDto.hijri?.date,
            hijriMonth = mosqueTimeDto.hijri?.month?.en,
            hijriYear = mosqueTimeDto.hijri?.year,
            weekdayAr = mosqueTimeDto.hijri?.weekday?.ar,
            weekdayEn = mosqueTimeDto.hijri?.weekday?.en,
            timestamp = mosqueTimeDto.timestamp,
            fajr = mosqueTimeDto.timingDetails?.fajr ?: "00:00",
            sunrise = mosqueTimeDto.sunrise ?: "00:00",
            dhuhr = mosqueTimeDto.timingDetails?.dhuhr ?: "00:00",
            asr = mosqueTimeDto.timingDetails?.asr ?: "00:00",
            maghrib = mosqueTimeDto.timingDetails?.maghrib ?: "00:00",
            isha = mosqueTimeDto.timingDetails?.isha ?: "00:00",
            sunset = mosqueTimeDto.sunset,
            imsak = mosqueTimeDto.timingDetails?.imsak,
            lastModified = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
    }

    fun mapMessageToEntity(
        messageKey: String,
        messageUrl: String,
        mosqueId: String,
        orderIndex: Int,
        localImagePath: String? = null
    ): MosqueMessageEntity {
        return MosqueMessageEntity(
            id = "${mosqueId}_$messageKey",
            mosqueId = mosqueId,
            messageKey = messageKey,
            messageUrl = messageUrl,
            localImagePath = localImagePath,
            orderIndex = orderIndex,
            lastModified = System.currentTimeMillis(),
            syncStatus = SyncStatus.SYNCED
        )
    }
}
