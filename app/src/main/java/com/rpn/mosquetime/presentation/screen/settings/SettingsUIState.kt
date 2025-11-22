package com.rpn.mosquetime.presentation.screen.settings

import com.rpn.mosquetime.presentation.screen.settings.composable.Theme

data class SettingsUIState(
    val error: String? = null,
    val isLoading: Boolean = false,
    val is12HourFormat: Boolean = true,
    val enablePrePrayerNotification: Boolean = true,
    val enablePostPrayerNotification: Boolean = true,
    val defaultFajrTime: String = "05:30",
    val defaultDhuhrTime: String = "12:00",
    val defaultAsrTime: String = "15:00",
    val defaultMaghribTime: String = "18:00",
    val defaultIshaTime: String = "19:30",
    val defaultJumahTime: String = "13:30",
    val defaultSunriseTime: String = "5:30",
    val selectedBackgroundImage: String = "",
    val availableBackgroundImages: List<String> = emptyList(),
    val showMosqueName: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userEmail: String = "",
    val userId: String = "",
    val mosqueId: String = "",
    val mosqueName: String = "",
    val mosqueMessage: String = "",
    val theme: Theme = Theme.SYSTEM
)
