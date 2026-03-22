package com.rpn.salatetime.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rpn.salatetime.domain.model.Mosque
import com.rpn.salatetime.domain.model.MosqueCompositeData
import com.rpn.salatetime.domain.model.PrayerName
import com.rpn.salatetime.domain.model.Settings
import com.rpn.salatetime.domain.model.Theme
import com.rpn.salatetime.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.io.IOException

// Extension property creates a single DataStore instance scoped to the Context.
private val Context.settingsDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "app_settings")

class SettingsRepository(private val context: Context) {

    // ── Key definitions ───────────────────────────────────────────────────────

    private object Keys {
        val SHOULD_SHOW_MOSQUE_NAME = booleanPreferencesKey("should_show_mosque_name")
        val IS_24_HOUR_FORMAT = booleanPreferencesKey("is_24_hour_format")
        val MOSQUE_NAME = stringPreferencesKey("mosque_name")
        val MESSAGE = stringPreferencesKey("message")
        val FAJR_TIME = stringPreferencesKey("fajr_time")
        val DHUHR_TIME = stringPreferencesKey("dhuhr_time")
        val ASR_TIME = stringPreferencesKey("asr_time")
        val MAGHRIB_TIME = stringPreferencesKey("maghrib_time")
        val ISHA_TIME = stringPreferencesKey("isha_time")
        val JUMAH_TIME = stringPreferencesKey("jumah_time")
        val SUNRISE_TIME = stringPreferencesKey("sunrise_time")
        val BACKGROUND_IMAGE = stringPreferencesKey("background_image")
        val PRE_PRAYER_NOTIFICATION = booleanPreferencesKey("pre_prayer_notification")
        val POST_PRAYER_NOTIFICATION = booleanPreferencesKey("post_prayer_notification")
        val USER_ID = stringPreferencesKey("user_id")
        val USER_EMAIL = stringPreferencesKey("user_email")
        val USER_NAME = stringPreferencesKey("user_name")
        val MOSQUE_ID = stringPreferencesKey("mosque_id")
        val LOCATION_LAT = doublePreferencesKey("location_lat")
        val LOCATION_LON = doublePreferencesKey("location_lon")
        val CALCULATION_METHOD = stringPreferencesKey("calculation_method")
        val MADHAB = stringPreferencesKey("madhab")
        val LANGUAGE = stringPreferencesKey("language")
        val THEME = stringPreferencesKey("theme")
    }

    // ── Observable stream ─────────────────────────────────────────────────────

    val settingsFlow: Flow<Settings> = context.settingsDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Timber.e(exception, "DataStore read error — emitting defaults")
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs -> prefs.toPrayerSettings() }

    // ── Write operations ──────────────────────────────────────────────────────

    suspend fun setTimeFormat(is24: Boolean) = save(Keys.IS_24_HOUR_FORMAT, is24)
    suspend fun setPrayerOffset(prayer: PrayerName, time: String) = save(prayer.prefKey(), time)
    suspend fun setPrayerTime(prayer: String, time: String) {
        when (prayer.lowercase()) {
            "fajr" -> save(Keys.FAJR_TIME, time)
            "dhuhr" -> save(Keys.DHUHR_TIME, time)
            "asr" -> save(Keys.ASR_TIME, time)
            "maghrib" -> save(Keys.MAGHRIB_TIME, time)
            "isha" -> save(Keys.ISHA_TIME, time)
            "jumah" -> save(Keys.JUMAH_TIME, time)
            "sunrise" -> save(Keys.SUNRISE_TIME, time)
        }
    }


    suspend fun setTheme(theme: Theme) = save(Keys.THEME, theme.name)
    suspend fun setBackgroundImage(path: String) = save(Keys.BACKGROUND_IMAGE, path)
    suspend fun setPrePrayerNotification(enabled: Boolean) =
        save(Keys.PRE_PRAYER_NOTIFICATION, enabled)

    suspend fun setMosqueNameVisibility(enabled: Boolean) =
        save(Keys.SHOULD_SHOW_MOSQUE_NAME, enabled)

    suspend fun setPostPrayerNotification(enabled: Boolean) =
        save(Keys.POST_PRAYER_NOTIFICATION, enabled)

    suspend fun setLocation(lat: Double, lon: Double) {
        save(Keys.LOCATION_LAT, lat)
        save(Keys.LOCATION_LON, lon)
    }

    suspend fun setLanguage(language: String) = save(Keys.LANGUAGE, language)

    suspend fun setMosque(mosqueInfo: Mosque) {
        mosqueInfo.apply {
            save(Keys.MOSQUE_ID, id)
            name?.let { save(Keys.MOSQUE_NAME, it) }
            jumuaTime?.let { save(Keys.JUMAH_TIME, it) }
            bottomMessage?.let { save(Keys.MESSAGE, it) }
            thumbnail?.let { save(Keys.BACKGROUND_IMAGE, it) }
        }
    }

    suspend fun setUser(id: String? = null, email: String? = null, userName: String? = null) {
        id?.let { save(Keys.USER_ID, it) }
        email?.let { save(Keys.USER_EMAIL, email) }
        userName?.let { save(Keys.USER_NAME, userName) }
    }




        /**
         * Optimized atomic update for all Mosque and Prayer data.
         * This performs a single disk write instead of multiple sequential ones.
         */
        suspend fun updateFromCompositeData(data: MosqueCompositeData) {
            context.settingsDataStore.edit { prefs ->
                // 1. Update Mosque Info
                data.mosque?.let { mosque ->
                    prefs[Keys.MOSQUE_ID] = mosque.id
                    mosque.name?.let { prefs[Keys.MOSQUE_NAME] = it }
                    mosque.bottomMessage?.let { prefs[Keys.MESSAGE] = it }

                    // Prioritize local path for performance, fallback to remote image
                    val bgImage = mosque.localPath.takeIf { !it.isNullOrBlank() } ?: mosque.image
                    bgImage?.let { prefs[Keys.BACKGROUND_IMAGE] = it }

                    // Jumah time logic
                    mosque.jumuaTime?.takeIf { it.isNotBlank() }?.let {
                        prefs[Keys.JUMAH_TIME] = it.toHHmm()
                    }
                }

                // 2. Update Prayer Times
                data.todayPrayerTime?.let { prayer ->
                    prefs[Keys.FAJR_TIME] = prayer.fajr.toHHmm()
                    prefs[Keys.DHUHR_TIME] = prayer.dhuhr.toHHmm()
                    prefs[Keys.ASR_TIME] = prayer.asr.toHHmm()
                    prefs[Keys.MAGHRIB_TIME] = prayer.maghrib.toHHmm()
                    prefs[Keys.ISHA_TIME] = prayer.isha.toHHmm()
                    prefs[Keys.SUNRISE_TIME] = prayer.sunrise.toHHmm()
                }
            }
        }




    // ── Helpers ───────────────────────────────────────────────────────────────

    /** "04:50:00" → "04:50"  |  "04:50" → "04:50" */
    private fun String.toHHmm(): String =
        split(":").take(2).joinToString(":").padStart(5, '0')
    private fun Preferences.toPrayerSettings() = Settings(
        shouldShowMosqueName = this[Keys.SHOULD_SHOW_MOSQUE_NAME] ?: true,
        mosqueName = this[Keys.MOSQUE_NAME] ?: Constants.DEFAULT_MOSQUE_NAME,
        message = this[Keys.MESSAGE] ?: Constants.DEFAULT_MOSQUE_MESSAGE,
        is24HourFormat = this[Keys.IS_24_HOUR_FORMAT] ?: true,
        fajrTime = this[Keys.FAJR_TIME] ?: "05:00",
        dhuhrTime = this[Keys.DHUHR_TIME] ?: "12:30",
        asrTime = this[Keys.ASR_TIME] ?: "15:45",
        maghribTime = this[Keys.MAGHRIB_TIME] ?: "18:20",
        ishaTime = this[Keys.ISHA_TIME] ?: "20:00",
        jumahTime = this[Keys.JUMAH_TIME] ?: "12:45",
        sunriseTime = this[Keys.SUNRISE_TIME] ?: "06:15",
        backgroundImage = this[Keys.BACKGROUND_IMAGE] ?: "",
        prePrayerNotification = this[Keys.PRE_PRAYER_NOTIFICATION] ?: true,
        postPrayerNotification = this[Keys.POST_PRAYER_NOTIFICATION] ?: true,
        mosqueId = this[Keys.MOSQUE_ID] ?: "",
        userId = this[Keys.USER_ID] ?: "", //Demo="fe0e1fb2-e00f-42df-b9d9-b87e188ace46" , // TODO: Remove it
        locationLatitude = this[Keys.LOCATION_LAT],
        locationLongitude = this[Keys.LOCATION_LON],
        calculationMethod = this[Keys.CALCULATION_METHOD] ?: "MWL",
        madhab = this[Keys.MADHAB] ?: "Shafi",
        language = this[Keys.LANGUAGE] ?: "en",
        theme = Theme.valueOf(this[Keys.THEME] ?: Theme.SYSTEM.name)
    )

    private fun PrayerName.prefKey(): Preferences.Key<String> = when (this) {
        PrayerName.SUNRISE -> Keys.SUNRISE_TIME
        PrayerName.FAJR -> Keys.FAJR_TIME
        PrayerName.DHUHR -> Keys.DHUHR_TIME
        PrayerName.ASR -> Keys.ASR_TIME
        PrayerName.MAGHRIB -> Keys.MAGHRIB_TIME
        PrayerName.ISHA -> Keys.ISHA_TIME
    }

    suspend fun clearUserData() {
        context.settingsDataStore.edit { prefs ->
            prefs.remove(Keys.MOSQUE_ID)
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.USER_EMAIL)
            prefs.remove(Keys.USER_NAME)
            prefs.remove(Keys.SHOULD_SHOW_MOSQUE_NAME)
            prefs.remove(Keys.IS_24_HOUR_FORMAT)
            prefs.remove(Keys.MOSQUE_NAME)
            prefs.remove(Keys.MESSAGE)
            prefs.remove(Keys.FAJR_TIME)
            prefs.remove(Keys.DHUHR_TIME)
            prefs.remove(Keys.ASR_TIME)
            prefs.remove(Keys.MAGHRIB_TIME)
            prefs.remove(Keys.ISHA_TIME)
            prefs.remove(Keys.JUMAH_TIME)
            prefs.remove(Keys.SUNRISE_TIME)
            prefs.remove(Keys.BACKGROUND_IMAGE)
            prefs.remove(Keys.PRE_PRAYER_NOTIFICATION)
            prefs.remove(Keys.POST_PRAYER_NOTIFICATION)
            prefs.remove(Keys.LOCATION_LAT)
            prefs.remove(Keys.LOCATION_LON)
            prefs.remove(Keys.CALCULATION_METHOD)
            prefs.remove(Keys.MADHAB)
            prefs.remove(Keys.LANGUAGE)
            prefs.remove(Keys.THEME)
        }
    }


    // Generic Save function
    private suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        context.settingsDataStore.edit { prefs ->
            prefs[key] = value
        }
    }
}

/** Sealed enum identifying each prayer for offset targeting. */

