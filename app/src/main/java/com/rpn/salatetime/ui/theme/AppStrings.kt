package com.rpn.salatetime.ui.theme


import android.app.Application
import android.content.Context
import androidx.annotation.StringRes
import com.rpn.salatetime.R

/**
 * Centralized string provider — injected as a Koin singleton.
 *
 * Holds [Application] context only (never Activity/Fragment), so there is
 * no memory leak. The class itself is not static — Koin owns the instance.
 *
 * Koin registration:
 *   single { AppStrings(androidApplication()) }
 *
 * Usage in ViewModel:
 *   class MyViewModel(private val strings: AppStrings) : ViewModel()
 *   strings.timeFormatChanged(true)
 */
class AppStrings(private val context: Context) {

    // ─────────────────────────────────────────────────────────────────────────
    // Generic accessor
    // ─────────────────────────────────────────────────────────────────────────

    fun get(@StringRes resId: Int): String = context.getString(resId)

    fun get(@StringRes resId: Int, vararg args: Any): String = context.getString(resId, *args)

    // ─────────────────────────────────────────────────────────────────────────
    // Settings strings
    // ─────────────────────────────────────────────────────────────────────────

    fun timeFormatChanged(is24: Boolean): String = get(
        if (is24) R.string.twenty_four_hour_format_settings else R.string.twelve_hour_format
    )

    fun mosqueNameVisibility(visible: Boolean): String = get(
        if (visible) R.string.mosque_name_visibility_on else R.string.mosque_name_visibility_off
    )

    fun prePrayerNotification(enabled: Boolean): String = get(
        if (enabled) R.string.pre_prayer_notification_enabled else R.string.pre_prayer_notification_disabled
    )

    fun postPrayerNotification(enabled: Boolean): String = get(
        if (enabled) R.string.post_prayer_notification_enabled else R.string.post_prayer_notification_disabled
    )

    fun prayerTimeUpdated(prayer: String, time: String): String =
        get(R.string.prayer_time_updated, prayer, time)

    fun backgroundImageUpdated(): String = get(R.string.background_image_updated)

    fun themeChanged(themeName: String): String   = get(R.string.theme_changed, themeName)

    fun languageChanged(displayName: String): String = get(R.string.language_changed, displayName)

    fun syncingData(): String   = get(R.string.syncing_data)
//    fun syncSuccess(): String   = get(R.string.synced_successfully)
//    fun syncFailed(reason: String): String = get(R.string.sync_failed, reason)
//    fun syncOffline(): String   = get(R.string.sync_offline_message)
//    fun prayerTimesUpdatedFromServer(): String = get(R.string.prayer_times_updated_from_server)
    fun loginFirst(): String    = get(R.string.please_login_first_to_sync_data)
    fun logoutSuccess(): String = get(R.string.logout_successful)

    // ─────────────────────────────────────────────────────────────────────────
    // Language display names
    // ─────────────────────────────────────────────────────────────────────────

    fun languageDisplayName(code: String): String = when (code.lowercase()) {
        "en" -> "English"
        "fr" -> "Français"
        "bn" -> "বাংলা"
        else -> "English"
    }
}