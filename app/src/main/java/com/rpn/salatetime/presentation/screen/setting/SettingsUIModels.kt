package com.rpn.salatetime.presentation.screen.setting

import com.rpn.salatetime.domain.model.MosqueCompositeData
import com.rpn.salatetime.domain.model.Theme

// ─────────────────────────────────────────────────────────────────────────────
// State
// ─────────────────────────────────────────────────────────────────────────────

data class SettingsUIState(
    val isLoading: Boolean = false,
    val error: String? = null,

    // Display
    val theme: Theme = Theme.SYSTEM,
    val selectedLanguage: String = "en",
    val showMosqueName: Boolean = false,
    val is12HourFormat: Boolean = false,   // false = 24-hour, true = 12-hour

    // Prayer times (fallback defaults — overwritten by DataStore immediately)
    val defaultFajrTime: String = "05:00",
    val defaultSunriseTime: String = "06:15",
    val defaultDhuhrTime: String = "12:30",
    val defaultAsrTime: String = "15:45",
    val defaultMaghribTime: String = "18:20",
    val defaultIshaTime: String = "20:00",
    val defaultJumahTime: String = "12:45",

    // Notifications
    val enablePrePrayerNotification: Boolean = true,
    val enablePostPrayerNotification: Boolean = true,

    // Background
    val selectedBackgroundImage: String = "",
    val availableBackgroundImages: List<String> = emptyList(),

    // Account
    val isLoggedIn: Boolean = false,
    val userId: String = "",
    val userEmail: String = "",
    val mosqueId: String = "",
    val mosqueName: String = "",
    val mosqueMessage: String = "",
    val mosqueData: MosqueCompositeData? = null,
)

// ─────────────────────────────────────────────────────────────────────────────
// Events
// ─────────────────────────────────────────────────────────────────────────────

sealed class SettingsUIEvent {
    data class ToggleTimeFormat(val is12Hour: Boolean) : SettingsUIEvent()
    data class ToggleShowMosqueName(val showMosqueName: Boolean) : SettingsUIEvent()
    data class TogglePrePrayerNotification(val enable: Boolean) : SettingsUIEvent()
    data class TogglePostPrayerNotification(val enable: Boolean) : SettingsUIEvent()
    data class UpdateDefaultPrayerTime(val prayer: String, val time: String) : SettingsUIEvent()
    data class SelectBackgroundImage(val imagePath: String) : SettingsUIEvent()
    data class SelectTheme(val theme: Theme) : SettingsUIEvent()
    data class SelectLanguage(val language: String) : SettingsUIEvent()
    data object LoadBackgroundImages : SettingsUIEvent()
    data object Login : SettingsUIEvent()
    data object Logout : SettingsUIEvent()
    data object SyncData : SettingsUIEvent()
}

// ─────────────────────────────────────────────────────────────────────────────
// Effects (one-off side effects)
// ─────────────────────────────────────────────────────────────────────────────

sealed class SettingsUIEffect {
    data class ShowToast(val message: String) : SettingsUIEffect()
    data object NavigateToLogin : SettingsUIEffect()
    data object NavigateToHome : SettingsUIEffect()
    data class ShowImagePicker(val images: List<String>) : SettingsUIEffect()
}
