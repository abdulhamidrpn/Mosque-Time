package com.rpn.mosquetime.model

import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.Gson
import java.io.Serializable
import java.util.*
import java.util.Date

data class MasjidInfo(
    @ServerTimestamp
    var creationDate: Date? = null,
    var activated: Boolean = false,
    var city: String = "",
    var country: String = "",
    var email: String = "",
    var image: String = "",
    var jumua: String = "",
    var latitude: String = "",
    var longitude: String = "",
    var masjidName: String = "",
    var ownerName: String = "",
    var ownerUid: String = "",
    var phoneNumber: String = "",
    var documentId: String = "",
    var bottomMessage: String = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
) : Serializable {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}
