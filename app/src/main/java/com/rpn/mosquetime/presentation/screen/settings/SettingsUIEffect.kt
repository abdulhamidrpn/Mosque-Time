package com.rpn.mosquetime.presentation.screen.settings

sealed class SettingsUIEffect {
    data class ShowToast(val message: String) : SettingsUIEffect()
    data object NavigateToLogin : SettingsUIEffect()
    data object NavigateToHome : SettingsUIEffect()
    data class ShowImagePicker(val images: List<String>) : SettingsUIEffect()
}