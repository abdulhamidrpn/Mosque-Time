package com.rpn.mosquetime.data.mapper

import com.rpn.mosquetime.data.local.entity.MosqueInfoEntity
import com.rpn.mosquetime.domain.Mapper
import com.rpn.mosquetime.domain.model.MasjidInfo

// Mosque Info Mapper
class MosqueInfoMapper : Mapper<MosqueInfoEntity, MasjidInfo> {

    override fun mapToDomain(entity: MosqueInfoEntity): MasjidInfo {
        return MasjidInfo(
            name = entity.masjidName,
            logoUrl = entity.localImagePath ?: entity.image, // Prefer local image
            thumbnail = entity.localImagePath ?: entity.image,
            address = buildAddress(entity.city, entity.country),
            website = entity.email, // Using email as website for now
            jumua = entity.jumua,
            message = entity.bottomMessage,
            imageMessages = emptyList(), // Will be populated separately
            prayerTimes = emptyList() // Will be populated separately
        )
    }

    override fun mapToEntity(domain: MasjidInfo): MosqueInfoEntity {
        // This is typically used for reverse mapping if needed
        return MosqueInfoEntity(
            documentId = "", // Should be provided separately
            masjidName = domain.name ?: "",
            ownerUid = "",
            activated = true,
            image = domain.logoUrl,
            jumua = domain.jumua,
            bottomMessage = domain.message ?: "",
            email = domain.website ?: ""
        )
    }

    private fun buildAddress(city: String, country: String): String {
        return when {
            city.isNotEmpty() && country.isNotEmpty() -> "$city, $country"
            city.isNotEmpty() -> city
            country.isNotEmpty() -> country
            else -> ""
        }
    }
}
