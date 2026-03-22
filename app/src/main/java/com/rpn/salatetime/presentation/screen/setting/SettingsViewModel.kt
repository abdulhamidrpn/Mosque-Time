package com.rpn.salatetime.presentation.screen.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rpn.salatetime.data.local.datastore.SettingsRepository
import com.rpn.salatetime.data.local.db.toDomain
import com.rpn.salatetime.data.repository.MosqueRepository
import com.rpn.salatetime.domain.model.MosqueCompositeData
import com.rpn.salatetime.domain.repository.ImageRepository
import com.rpn.salatetime.ui.theme.AppStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val mosqueRepository: MosqueRepository,
    private val imageRepository: ImageRepository,
    private val appStrings: AppStrings,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUIState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<SettingsUIEffect>(replay = 0)
    val uiEffect: SharedFlow<SettingsUIEffect> = _uiEffect.asSharedFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { s ->
                _uiState.update {
                    it.copy(
                        is12HourFormat = s.is24HourFormat,
                        showMosqueName = s.shouldShowMosqueName,
                        mosqueName = s.mosqueName,
                        mosqueMessage = s.message,
                        defaultFajrTime = s.fajrTime,
                        defaultDhuhrTime = s.dhuhrTime,
                        defaultAsrTime = s.asrTime,
                        defaultMaghribTime = s.maghribTime,
                        defaultIshaTime = s.ishaTime,
                        defaultJumahTime = s.jumahTime,
                        defaultSunriseTime = s.sunriseTime,
                        selectedBackgroundImage = s.backgroundImage,
                        enablePrePrayerNotification = s.prePrayerNotification,
                        enablePostPrayerNotification = s.postPrayerNotification,
                        isLoggedIn = s.userId.isNotEmpty(),
                        userEmail = s.userEmail,
                        userId = s.userId,
                        mosqueId = s.mosqueId,
                        theme = s.theme,
                        selectedLanguage = s.language,
                    )
                }
            }
        }
    }

    fun onEvent(event: SettingsUIEvent) {
        viewModelScope.launch {
            when (event) {
                is SettingsUIEvent.ToggleTimeFormat -> {
                    settingsRepository.setTimeFormat(event.is12Hour)
                    toast(appStrings.timeFormatChanged(is24 = event.is12Hour))
                }

                is SettingsUIEvent.ToggleShowMosqueName -> {
                    settingsRepository.setMosqueNameVisibility(event.showMosqueName)
                    toast(appStrings.mosqueNameVisibility(event.showMosqueName))
                }

                is SettingsUIEvent.TogglePrePrayerNotification -> {
                    settingsRepository.setPrePrayerNotification(event.enable)
                    toast(appStrings.prePrayerNotification(event.enable))
                }

                is SettingsUIEvent.TogglePostPrayerNotification -> {
                    settingsRepository.setPostPrayerNotification(event.enable)
                    toast(appStrings.postPrayerNotification(event.enable))
                }

                is SettingsUIEvent.UpdateDefaultPrayerTime -> {
                    settingsRepository.setPrayerTime(event.prayer, event.time)
                    toast(appStrings.prayerTimeUpdated(event.prayer, event.time))
                }

                is SettingsUIEvent.SelectBackgroundImage -> {
                    val path = cacheImageIfNeeded(event.imagePath)
                    settingsRepository.setBackgroundImage(path)
                    toast(appStrings.backgroundImageUpdated())
                }

                is SettingsUIEvent.SelectTheme -> {
                    settingsRepository.setTheme(event.theme)
                    toast(appStrings.themeChanged(event.theme.name))
                }

                is SettingsUIEvent.SelectLanguage -> {
                    settingsRepository.setLanguage(event.language)
                    toast(appStrings.languageChanged(appStrings.languageDisplayName(event.language)))
                }

                SettingsUIEvent.LoadBackgroundImages ->
                    _uiState.update { it.copy(availableBackgroundImages = defaultBackgroundImages) }

                SettingsUIEvent.Login -> emit(SettingsUIEffect.NavigateToLogin)
                SettingsUIEvent.Logout -> {
                    toast(appStrings.logoutSuccess())
                }

                SettingsUIEvent.SyncData -> {
                    if (_uiState.value.isLoggedIn) {
                        toast(appStrings.syncingData())
                        syncFromNetwork()
                    } else toast(appStrings.loginFirst())
                }
            }
        }
    }

    private fun syncFromNetwork() {
        // Implementation here if needed
    }

    private suspend fun persistMosqueInfoToSettings(data: MosqueCompositeData) {
        data.mosque?.toDomain()?.let {
            settingsRepository.setMosque(it)
        }
    }

    private suspend fun cacheImageIfNeeded(path: String): String {
        if (path.isBlank()) return path
        return runCatching {
            withContext(Dispatchers.IO) {
                imageRepository.cacheImage(path)?.absolutePath ?: path
            }
        }
            .onFailure { Timber.e(it, "Failed to cache image") }.getOrDefault(path)
    }

    private suspend fun emit(effect: SettingsUIEffect) = _uiEffect.emit(effect)
    private suspend fun toast(msg: String) = emit(SettingsUIEffect.ShowToast(msg))

    private val defaultBackgroundImages = listOf(
        "https://i.ibb.co.com/RGbJZV6V/background-mosque.jpg",
        "https://images.unsplash.com/photo-1512632578888-169bbbc64f33?q=80&w=2070",
        "https://images.unsplash.com/photo-1512970648279-ff3398568f77?q=80&w=2076",
        "https://images.unsplash.com/photo-1590092794015-bce5431c83f4?q=80&w=2011",
        "https://images.unsplash.com/photo-1570715746786-e7ca46b1e50b?q=80&w=1974",
        "https://images.unsplash.com/photo-1560626184-524744344bef?q=80&w=2133",
        "https://images.unsplash.com/photo-1607398202930-f4c68d729323?q=80&w=2071",
    )
}
