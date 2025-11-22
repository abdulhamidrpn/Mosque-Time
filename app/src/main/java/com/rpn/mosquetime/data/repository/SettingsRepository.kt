package com.rpn.mosquetime.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rpn.mosquetime.domain.model.MasjidInfo
import com.rpn.mosquetime.domain.model.Settings
import com.rpn.mosquetime.presentation.screen.settings.composable.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val DEFAULT_MOSQUE_NAME = "Masjid al-Noor"
val DEFAULT_MOSQUE_MESSAGE =
    "Welcome to the House of Allah – Please maintain silence and respect for worship."

// Extension property for DataStore
val Context.settingsDataStore by preferencesDataStore(name = "app_settings")

class SettingsRepository(private val context: Context) {

    // Define keys
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

    // Expose Flow<Settings>
    val settingsFlow: Flow<Settings> = context.settingsDataStore.data.map { prefs ->
        Settings(
            shouldShowMosqueName = prefs[Keys.SHOULD_SHOW_MOSQUE_NAME] ?: true,
            mosqueName = prefs[Keys.MOSQUE_NAME] ?: DEFAULT_MOSQUE_NAME,
            message = prefs[Keys.MESSAGE] ?: DEFAULT_MOSQUE_MESSAGE,
            is24HourFormat = prefs[Keys.IS_24_HOUR_FORMAT] ?: true,
            fajrTime = prefs[Keys.FAJR_TIME] ?: "05:00",
            dhuhrTime = prefs[Keys.DHUHR_TIME] ?: "12:30",
            asrTime = prefs[Keys.ASR_TIME] ?: "15:45",
            maghribTime = prefs[Keys.MAGHRIB_TIME] ?: "18:20",
            ishaTime = prefs[Keys.ISHA_TIME] ?: "20:00",
            jumahTime = prefs[Keys.JUMAH_TIME] ?: "12:45",
            sunriseTime = prefs[Keys.SUNRISE_TIME] ?: "06:15",
            backgroundImage = prefs[Keys.BACKGROUND_IMAGE] ?: "",
            prePrayerNotification = prefs[Keys.PRE_PRAYER_NOTIFICATION] ?: true,
            postPrayerNotification = prefs[Keys.POST_PRAYER_NOTIFICATION] ?: true,
            mosqueId = prefs[Keys.MOSQUE_ID] ?: "",
            userId = prefs[Keys.USER_ID] ?: "",
            locationLatitude = prefs[Keys.LOCATION_LAT],
            locationLongitude = prefs[Keys.LOCATION_LON],
            calculationMethod = prefs[Keys.CALCULATION_METHOD] ?: "MWL",
            madhab = prefs[Keys.MADHAB] ?: "Shafi",
            language = prefs[Keys.LANGUAGE] ?: "en",
            theme = Theme.valueOf(prefs[Keys.THEME] ?: Theme.SYSTEM.name)
        )
    }

    // Generic Save function
    private suspend fun <T> save(key: Preferences.Key<T>, value: T) {
        context.settingsDataStore.edit { prefs ->
            prefs[key] = value
        }
    }

    // Save individual settings
    suspend fun setMosqueNameVisibility(show: Boolean) = save(Keys.SHOULD_SHOW_MOSQUE_NAME, show)
    suspend fun setTimeFormat(is24: Boolean) = save(Keys.IS_24_HOUR_FORMAT, is24)
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

    suspend fun setPostPrayerNotification(enabled: Boolean) =
        save(Keys.POST_PRAYER_NOTIFICATION, enabled)

    suspend fun setLocation(lat: Double, lon: Double) {
        save(Keys.LOCATION_LAT, lat)
        save(Keys.LOCATION_LON, lon)
    }

    suspend fun setMosque(mosqueInfo: MasjidInfo) {
        mosqueInfo.apply {
            name?.let { save(Keys.MOSQUE_NAME, it) }
            message?.let { save(Keys.MESSAGE, it) }
            thumbnail?.let { save(Keys.BACKGROUND_IMAGE, it) }
        }
    }

    suspend fun setMosqueId(id: String) = save(Keys.MOSQUE_ID, id)
    suspend fun setUser(id: String, email: String, userName: String) {
        save(Keys.USER_ID, id)
        save(Keys.USER_EMAIL, email)
        save(Keys.USER_NAME, userName)
    }

    suspend fun setCalculationMethod(method: String) = save(Keys.CALCULATION_METHOD, method)
    suspend fun setMadhab(madhab: String) = save(Keys.MADHAB, madhab)
    suspend fun setLanguage(language: String) = save(Keys.LANGUAGE, language)
    suspend fun clearUserData() {
        context.settingsDataStore.edit { prefs ->
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
            prefs.remove(Keys.MOSQUE_ID)
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.USER_EMAIL)
            prefs.remove(Keys.USER_NAME)
            prefs.remove(Keys.LOCATION_LAT)
            prefs.remove(Keys.LOCATION_LON)
            prefs.remove(Keys.CALCULATION_METHOD)
            prefs.remove(Keys.MADHAB)
            prefs.remove(Keys.LANGUAGE)
            prefs.remove(Keys.THEME)
        }
    }
}
