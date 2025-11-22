package com.rpn.mosquetime.presentation.screen.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rpn.mosquetime.R
import com.rpn.mosquetime.data.repository.SettingsRepository
import com.rpn.mosquetime.domain.model.MasjidInfo
import com.rpn.mosquetime.domain.repository.MainRepository
import com.rpn.mosquetime.utils.AppStringProvider
import com.rpn.mosquetime.utils.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val dataStore: SettingsRepository,
    private val repository: MainRepository,
    private val stringProvider: AppStringProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUIState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<SettingsUIEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            dataStore.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(
                    is12HourFormat = !settings.is24HourFormat,
                    mosqueName = settings.mosqueName,
                    mosqueMessage = settings.message,
                    defaultFajrTime = settings.fajrTime,
                    defaultDhuhrTime = settings.dhuhrTime,
                    defaultAsrTime = settings.asrTime,
                    defaultMaghribTime = settings.maghribTime,
                    defaultIshaTime = settings.ishaTime,
                    defaultJumahTime = settings.jumahTime,
                    defaultSunriseTime = settings.sunriseTime,
                    selectedBackgroundImage = settings.backgroundImage,
                    showMosqueName = settings.shouldShowMosqueName,
                    enablePrePrayerNotification = settings.prePrayerNotification,
                    enablePostPrayerNotification = settings.postPrayerNotification,
                    isLoggedIn = settings.userId.isNotEmpty(),
                    userEmail = settings.userEmail, // or pull separately if you have email
                    userId = settings.userId, // or pull separately if you have email
                    theme = settings.theme
                )
            }
        }
    }

    fun onEvent(event: SettingsUIEvent) {
        when (event) {
            is SettingsUIEvent.ToggleTimeFormat -> {
                viewModelScope.launch {
                    dataStore.setTimeFormat(event.is12Hour)
                    _uiEffect.emit(
                        SettingsUIEffect.ShowToast(
                            stringProvider.getString(if (event.is12Hour) R.string.twelve_hour_format else R.string.twenty_four_hour_format_settings)
                        )
                    )
                }
            }

            is SettingsUIEvent.ToggleShowMosqueName -> {
                viewModelScope.launch {
                    dataStore.setMosqueNameVisibility(event.showMosqueName)
                    _uiEffect.emit(
                        SettingsUIEffect.ShowToast(
                            stringProvider.getString(if (event.showMosqueName) R.string.mosque_name_visibility_on else R.string.mosque_name_visibility_off)
                        )
                    )
                }
            }

            is SettingsUIEvent.TogglePrePrayerNotification -> {
                viewModelScope.launch {
                    dataStore.setPrePrayerNotification(event.enable)
                    _uiEffect.emit(
                        SettingsUIEffect.ShowToast(
                            stringProvider.getString(if (event.enable) R.string.pre_prayer_notification_enabled else R.string.pre_prayer_notification_disabled)
                        )
                    )
                }
            }

            is SettingsUIEvent.TogglePostPrayerNotification -> {
                viewModelScope.launch {
                    dataStore.setPostPrayerNotification(event.enable)
                    _uiEffect.emit(
                        SettingsUIEffect.ShowToast(
                            stringProvider.getString(if (event.enable) R.string.post_prayer_notification_enabled else R.string.post_prayer_notification_disabled)
                        )
                    )
                }
            }

            is SettingsUIEvent.UpdateDefaultPrayerTime -> {
                viewModelScope.launch {
                    dataStore.setPrayerTime(event.prayer, event.time)
                    _uiEffect.emit(
                        SettingsUIEffect.ShowToast(
                            stringProvider.getString(
                                R.string.prayer_time_updated,
                                event.prayer,
                                event.time
                            )
                        )
                    )
                }
            }

            is SettingsUIEvent.SelectBackgroundImage -> {
                viewModelScope.launch {
                    dataStore.setBackgroundImage(event.imagePath)
                    _uiEffect.emit(SettingsUIEffect.ShowToast(stringProvider.getString(R.string.background_image_updated)))
                }
            }

            is SettingsUIEvent.SelectTheme -> {
                viewModelScope.launch {
                    dataStore.setTheme(event.theme)
                    _uiEffect.emit(
                        SettingsUIEffect.ShowToast(
                            stringProvider.getString(
                                R.string.theme_changed,
                                event.theme.name
                            )
                        )
                    )
                }
            }

            SettingsUIEvent.LoadBackgroundImages -> {
                val images = listOf(
                    "https://images.unsplash.com/photo-1512632578888-169bbbc64f33?q=80&w=2070",
                    "https://images.unsplash.com/photo-1512970648279-ff3398568f77?q=80&w=2076",
                    "https://images.unsplash.com/photo-1590092794015-bce5431c83f4?q=80&w=2011",
                    "https://images.unsplash.com/photo-1570715746786-e7ca46b1e50b?q=80&w=1974",
                    "https://images.unsplash.com/photo-1560626184-524744344bef?q=80&w=2133",
                    "https://images.unsplash.com/photo-1607398202930-f4c68d729323?q=80&w=2071"
                )
                _uiState.value = _uiState.value.copy(availableBackgroundImages = images)
            }

            SettingsUIEvent.Login -> {
                viewModelScope.launch {
                    _uiEffect.emit(SettingsUIEffect.NavigateToLogin)
                }
            }

            SettingsUIEvent.Logout -> {
                viewModelScope.launch {
                    dataStore.setUser("", "", "") // clear login
                    _uiEffect.emit(SettingsUIEffect.ShowToast(stringProvider.getString(R.string.logout_successful)))
                }
            }

            SettingsUIEvent.SyncData -> {
                viewModelScope.launch {
                    if (_uiState.value.isLoggedIn) {
                        _uiEffect.emit(SettingsUIEffect.ShowToast(stringProvider.getString(R.string.syncing_data)))
                        repository.syncUser(_uiState.value.userId)
                        if (_uiState.value.mosqueId.isNotEmpty()) {
                            loadCompleteMosqueData(_uiState.value.mosqueId)
                        }
                    } else {
                        _uiEffect.emit(SettingsUIEffect.ShowToast(stringProvider.getString(R.string.please_login_first_to_sync_data)))
                    }
                }
            }
        }
    }


    fun loadCompleteMosqueData(mosqueId: String) {
        viewModelScope.launch {
            Log.d("TAGTRACKER", "loadCompleteMosqueData: ViewModel")
            repository.getCompleteMosqueInfo(mosqueId).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }

                    is Result.Success -> {
                        result.data?.let { mosqueInfo ->
                            updateInfoInSettings(mosqueInfo)
                        }
                    }

                    is Result.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }


            repository.syncStatus.collect {
                Log.d("TAG", "loadCompleteMosqueData: Sync status: $it")
            }

        }
    }


    private fun updateInfoInSettings(mosqueInfo: MasjidInfo) {
        viewModelScope.launch {
            dataStore.apply {
                setMosque(mosqueInfo)
            }
        }
        _uiState.update {
            it.copy(isLoading = false)
        }
    }

}