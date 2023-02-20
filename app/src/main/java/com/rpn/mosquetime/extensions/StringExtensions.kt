package com.rpn.mosquetime.extensions

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.rpn.mosquetime.utils.GeneralUtils
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec


fun String.convertToList(): MutableList<String?>? {

    val gson = Gson()
    val jsonText: String = this
    val text = gson.fromJson(
        jsonText,
        Array<String>::class.java
    )
    if (text == null)
        return mutableListOf()

    return text.toMutableList()
}

fun <T> MutableList<T>?.convertToString(): String {
    val gson = Gson()
    val textList: MutableList<String> = this as MutableList<String>
    val jsonText = gson.toJson(textList)
    return jsonText
}

fun convertListToString(textList: MutableList<String?>?): String {
    if (textList == null || textList.isEmpty()) return ""
    val gson = Gson()
    val jsonText = gson.toJson(textList)

    Log.d("TAG", "convertListToString: jsonText $jsonText")
    return jsonText
}

fun String?.getHijriMonthFormat(): String? {
    return this?.split("-")?.get(1)
}

fun String?.getLong(format24: Boolean = true): Long {
    return if (format24) GeneralUtils.sdfTime24.parse(this).time
    else GeneralUtils.sdfTimeAm.parse(this).time


}

fun String?.getMinute(format24: Boolean = true): Int? {
    return if (format24) this?.let { GeneralUtils.sdfTime24.parse(it).minutes }
    else this?.let { GeneralUtils.sdfTimeAm.parse(it).minutes }


}

fun String?.getHour(format24: Boolean = true): Int? {
    return if (format24) this?.let { GeneralUtils.sdfTime24.parse(it).hours }
    else this?.let { GeneralUtils.sdfTimeAm.parse(it).hours }
}

fun String?.getDiffrenceMinute(date2: String? = null, format24: Boolean = true): Int {
    val date1 = this?.let { GeneralUtils.sdfTime24.parse(it) }
    val date2 = date2?.let { GeneralUtils.sdfTime24.parse(it) }
    val diff: Long? = date1?.getTime()?.minus(date2?.getTime()!!)

    val seconds = diff?.div(1000)
    val minutes = seconds?.div(60)
    val hours = minutes?.div(60)
    val days = hours?.div(24)
    Log.d("TAG", "getDiffrenceMinute: $date1 date ${date2}\nDiff $diff\nminutes $minutes")
    return minutes?.toInt() ?: 0
//    return if (format24) this?.let { GeneralUtils.sdfTime24.parse(it) }
//    else this?.let { GeneralUtils.sdfTimeAm.parse(it).hours }
}

fun String?.convertLongToTime(hours: String? = null, minute: String? = null): Date {
    val timestamp = this?.toLong()?.times(1000)
    Log.d("TAG", "convertLongToTime: str $this timeStamp $timestamp")
    val date = timestamp?.let { Date(it) }
    val updatedDate =
        date?.setDayTime(hours?.toInt(), minute?.toInt()) ?: Calendar.getInstance().time
    val format1 = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
    val format2 = SimpleDateFormat("dd/M/yyyy HH:mm:ss")
    Log.d("TAG", "convertLongToTime: $format1 $format2")
    return updatedDate
}

fun Date.setDayTime(
    hourOfDay: Int? = null,
    minuteOfHour: Int? = null,
    secondOfMinute: Int? = null
): Date {
    val calendar = Calendar.getInstance()
    calendar.setTimeInMillis(this.time)
    if (hourOfDay != null) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
    }
    if (minuteOfHour != null) {
        calendar.set(Calendar.MINUTE, minuteOfHour)
    }
    if (secondOfMinute != null) {
        calendar.set(Calendar.SECOND, secondOfMinute);
    }
    return calendar.time
}

fun Date.getMinuteDefferance(date2: Date?): Int {
    val diff: Long = this.getTime() - if (date2 != null) date2.getTime() else 0L
    var seconds = diff / 1000
//    val minutes = if (seconds.div(60) %= 0L) seconds / 60 else seconds / 59
//    seconds++
    seconds = if (seconds > 0) seconds + 1 else seconds - 1
    val minutes = seconds / 60
    Log.d(
        "TAG",
        "getMinuteDefferance: Current Time $this to Date $date2 // seconds -> $seconds // minutes -> $minutes"
    )
//    val hours = minutes / 60
//    val days = hours / 24
    return minutes.toInt()
}


fun String.encrypt(context: Context): ByteArray {
    val plainText = this.toByteArray(Charsets.UTF_8)
    val keygen = KeyGenerator.getInstance("AES")
    keygen.init(256)
    val key = keygen.generateKey()
    saveSecretKey(context, key)
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.ENCRYPT_MODE, key)
    val cipherText = cipher.doFinal(plainText)
    saveInitializationVector(context, cipher.iv)

    val sb = StringBuilder()
    for (b in cipherText) {
        sb.append(b.toChar())
    }
    try {
        Toast.makeText(context, "dbg encrypted = [" + sb.toString() + "]", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return cipherText
}

fun ByteArray.decrypt(context: Context): String {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    val ivSpec = IvParameterSpec(getSavedInitializationVector(context))
    cipher.init(Cipher.DECRYPT_MODE, getSavedSecretKey(context), ivSpec)
    val cipherText = cipher.doFinal(this)

    val sb = StringBuilder()
    for (b in cipherText) {
        sb.append(b.toChar())
    }
    try {
        Toast.makeText(context, "dbg decrypted = [" + sb.toString() + "]", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
//    return cipherText
    return sb.toString()
}

fun saveSecretKey(context: Context, secretKey: SecretKey) {
    val baos = ByteArrayOutputStream()
    val oos = ObjectOutputStream(baos)
    oos.writeObject(secretKey)
    val strToSave =
        String(android.util.Base64.encode(baos.toByteArray(), android.util.Base64.DEFAULT))
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = sharedPref.edit()
    editor.putString("secret_key", strToSave)
    editor.apply()
}

fun getSavedSecretKey(context: Context): SecretKey {
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    val strSecretKey = sharedPref.getString("secret_key", "")
    val bytes = android.util.Base64.decode(strSecretKey, android.util.Base64.DEFAULT)
    val ois = ObjectInputStream(ByteArrayInputStream(bytes))
    val secretKey = ois.readObject() as SecretKey
    return secretKey
}

fun saveInitializationVector(context: Context, initializationVector: ByteArray) {
    val baos = ByteArrayOutputStream()
    val oos = ObjectOutputStream(baos)
    oos.writeObject(initializationVector)
    val strToSave =
        String(android.util.Base64.encode(baos.toByteArray(), android.util.Base64.DEFAULT))
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    val editor = sharedPref.edit()
    editor.putString("initialization_vector", strToSave)
    editor.apply()
}

fun getSavedInitializationVector(context: Context): ByteArray {
    val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    val strInitializationVector = sharedPref.getString("initialization_vector", "")
    val bytes = android.util.Base64.decode(strInitializationVector, android.util.Base64.DEFAULT)
    val ois = ObjectInputStream(ByteArrayInputStream(bytes))
    val initializationVector = ois.readObject() as ByteArray
    return initializationVector
}


//todo Extract File Name from URL -> %2F1000007033?alt
fun String.extractFileNameFromImageUri(): String {
    return try {
        this.split("%2FMOSQUE_MESSAGE%2F").last().split("?alt").first()
    } catch (e: Exception) {
        return try {
            File(this).name
        } catch (e: Exception) {
            this
        }
    }

}