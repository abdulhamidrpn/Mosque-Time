package com.rpn.salatetime.utils

/**
 * Localization helper for prayer notification messages
 * Supports: English (en), French (fr), Bengali (bn)
 */
object LocalizationHelper {

    enum class Language {
        EN, FR, BN
    }

    fun getLanguageFromCode(code: String): Language = when (code.lowercase()) {
        "fr" -> Language.FR
        "bn" -> Language.BN
        else -> Language.EN // Default to English
    }

    /**
     * Get localized message for prayer that has passed
     * @param prayerName Name of the prayer (e.g., "Fajr", "Dhuhr")
     * @param minutesPassed Number of minutes that have passed
     * @param language Language code (en, fr, bn)
     */
    fun getPrayerPassedMessage(
        prayerName: String,
        minutesPassed: Long,
        language: String
    ): String {
        val lang = getLanguageFromCode(language)
        return when (lang) {
            Language.FR -> {
                val prayerFr = prayerNameToFrench(prayerName)
                "La prière de $prayerFr a eu lieu il y a $minutesPassed minutes"
            }

            Language.BN -> {
                val prayerBn = prayerNameToBengali(prayerName)
                "  $prayerBn সালাত $minutesPassed মিনিট আগে সম্পন্ন হয়েছে"
            }

            Language.EN -> {
                "${prayerName
                        .lowercase()
                        .replaceFirstChar { it.uppercase() }
                } prayer has passed by $minutesPassed minutes"
            }
        }
    }

    /**
     * Get localized message for prayer that is approaching
     * @param prayerName Name of the prayer (e.g., "Fajr", "Dhuhr")
     * @param minutesTo Number of minutes until prayer
     * @param language Language code (en, fr, bn)
     */
    fun getPrayerApproachingMessage(
        prayerName: String,
        minutesTo: Long,
        language: String
    ): String {
        val lang = getLanguageFromCode(language)
        return when (lang) {
            Language.FR -> {
                val prayerFr = prayerNameToFrench(prayerName)
                if (minutesTo == 1L) getSilencePhoneMessage(language)
                else "La prière de $prayerFr commencera dans $minutesTo minutes"
            }

            Language.BN -> {
                val prayerBn = prayerNameToBengali(prayerName)
                if (minutesTo == 1L) getSilencePhoneMessage(language)
                else "$prayerBn সালাত $minutesTo মিনিটে শুরু হবে"
            }

            Language.EN -> {
                if (minutesTo == 1L) getSilencePhoneMessage(language)
                else "$prayerName prayer will start in $minutesTo minutes"
            }
        }
    }

    /**
     * Get localized "Please silence your phone" message
     * @param language Language code (en, fr, bn)
     * @return Localized silence message
     */
    fun getSilencePhoneMessage(language: String): String {
        val lang = getLanguageFromCode(language)
        return when (lang) {
            Language.FR -> "Veuillez silencer votre téléphone"
            Language.BN -> "অনুগ্রহ করে আপনার ফোন নিরব করুন"
            Language.EN -> "Please silence your phone"
        }
    }

    /**
     * Convert English prayer name to French
     */
    private fun prayerNameToFrench(prayerName: String): String = when (prayerName.lowercase()) {
        "fajr", "fajar" -> "Fajr"
        "dhuhr", "zuhr" -> "Dhuhr"
        "asr" -> "Asr"
        "maghrib" -> "Maghrib"
        "isha", "ishaa" -> "Isha"
        "jumah", "jummah" -> "Jummah"
        "sunrise" -> "Lever du soleil"
        else -> prayerName
    }

    /**
     * Convert English prayer name to Bengali
     */
    private fun prayerNameToBengali(prayerName: String): String = when (prayerName.lowercase()) {
        "fajr", "fajar" -> "ফজর"
        "dhuhr", "zuhr" -> "জোহর"
        "asr" -> "আসর"
        "maghrib" -> "মাগরিব"
        "isha", "ishaa" -> "ইশা"
        "jumah", "jummah" -> "জুমা'আ"
        "sunrise" -> "সূর্যোদয়"
        else -> prayerName
    }
}
