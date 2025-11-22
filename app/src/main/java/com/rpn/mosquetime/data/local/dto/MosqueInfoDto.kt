package com.rpn.mosquetime.data.local.dto

import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.Gson
import io.ktor.serialization.kotlinx.KotlinxSerializationConverter
import java.io.Serializable
import java.util.Date


data class MosqueInfoDto(
    @ServerTimestamp
    var creationDate: Date? = null,
    val documentId: String = "",
    val masjidName: String = "",
    val ownerUid: String = "",
    val ownerName: String = "",
    val activated: Boolean = false,
    val city: String = "",
    val country: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val image: String? = "",
    val jumua: String = "00:00",
    val bottomMessage: String = ""
) : Serializable {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
