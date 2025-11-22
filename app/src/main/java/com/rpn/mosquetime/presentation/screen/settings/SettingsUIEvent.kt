package com.rpn.mosquetime.presentation.screen.settings

import com.rpn.mosquetime.presentation.screen.settings.composable.Theme

sealed class SettingsUIEvent {
    data class ToggleTimeFormat(val is12Hour: Boolean) : SettingsUIEvent()
    data class ToggleShowMosqueName(val showMosqueName: Boolean) : SettingsUIEvent()
    data class TogglePrePrayerNotification(val enable: Boolean) : SettingsUIEvent()
    data class TogglePostPrayerNotification(val enable: Boolean) : SettingsUIEvent()
    data class UpdateDefaultPrayerTime(val prayer: String, val time: String) : SettingsUIEvent()
    data class SelectBackgroundImage(val imagePath: String) : SettingsUIEvent()
    data class SelectTheme(val theme: Theme) : SettingsUIEvent()
    data object LoadBackgroundImages : SettingsUIEvent()
    data object Login : SettingsUIEvent()
    data object Logout : SettingsUIEvent()
    data object SyncData : SettingsUIEvent()
}
