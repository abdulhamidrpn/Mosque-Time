package com.rpn.mosquetime.presentation.screen.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rpn.mosquetime.R
import com.rpn.mosquetime.data.repository.SettingsRepository
import com.rpn.mosquetime.domain.model.MasjidInfo
import com.rpn.mosquetime.domain.model.Timings
import com.rpn.mosquetime.domain.model.time.PrayerTime
import com.rpn.mosquetime.domain.repository.MainRepository
import com.rpn.mosquetime.domain.repository.TimeRepository
import com.rpn.mosquetime.utils.AppStringProvider
import com.rpn.mosquetime.utils.Result
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: MainRepository,
    private val timeRepo: TimeRepository,
    private val settingsUtility: SettingsRepository,
    private val stringProvider: AppStringProvider
) : ViewModel() {
    private val TAG = "MainViewModel"
    private val _state = MutableStateFlow(MainScreenState())
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    private val _effect = Channel<MainScreenEffect>()
    val effect = _effect.receiveAsFlow()

    init {
        setupFlows()
        observeSettings()
    }

    private fun observeSettings() {
        viewModelScope.launch {
            settingsUtility.settingsFlow.collect { settings ->

                _state.value = _state.value.copy(
                    timings = Timings(
                        fajr = settings.fajrTime,
                        dhuhr = settings.dhuhrTime,
                        asr = settings.asrTime,
                        maghrib = settings.maghribTime,
                        isha = settings.ishaTime,
                        sunrise = settings.sunriseTime,
                    ),
                    jummaTiming = settings.jumahTime,
                    selectedBackgroundImage = settings.backgroundImage,
                    is24HourFormat = !settings.is24HourFormat,
                    showMosqueName = settings.shouldShowMosqueName,
                    mosqueMessage = settings.message,
                    mosqueName = settings.mosqueName
                )
                updatePrayerTimings(_state.value.timings)
            }
        }
    }

    fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.Refresh -> {
                //loadCompleteMosqueData("86ohJRfbniiVd3vapwHL")
                //getMosqueData()
            }

            is MainScreenEvent.ShowSnackBar -> viewModelScope.launch {
                _effect.send(MainScreenEffect.ShowToast(event.message))
            }

            MainScreenEvent.OnMoreClick -> viewModelScope.launch {
                _effect.send(MainScreenEffect.OpenMore)
            }

            MainScreenEvent.OnQrClick -> viewModelScope.launch {
                _state.update { it.copy(showNotificationBanner = true) }
            }

            MainScreenEvent.Tick -> {} // ticks are handled by clockFlow

            MainScreenEvent.DismissNotificationBanner -> {
                _state.update { it.copy(showNotificationBanner = false) }
            }

            is MainScreenEvent.AddNotification -> {
                val currentNotifications = _state.value.notifications.toMutableList()
                currentNotifications.add(event.notification)
                _state.update {
                    it.copy(
                        isNavigationTriggered = true,
                        notifications = currentNotifications,
                    )
                }
                viewModelScope.launch {
                    _effect.send(MainScreenEffect.ShowNotification(event.notification))
                }
            }

            is MainScreenEvent.RemoveNotification -> {
                val currentNotifications = _state.value.notifications.filter { it.id != event.id }
                _state.update {
                    it.copy(
                        notifications = currentNotifications,
                        showNotificationBanner = false
                    )
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
                        _state.update { it.copy(isLoading = true) }
                    }

                    is Result.Success -> {
                        result.data?.let { mosqueInfo ->
                            updateInfoInSettings(mosqueInfo)
                            _state.update {
                                it.copy(isLoading = false, masjidInfo = mosqueInfo)
                            }
                        }
                    }

                    is Result.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }


            repository.syncStatus.collect {
                Log.d(TAG, "loadCompleteMosqueData: Sync status: $it")
            }

        }
    }

    private fun updateInfoInSettings(mosqueInfo: MasjidInfo) {

        val timings = mosqueInfo.prayerTimes.firstOrNull {
            it.date == timeRepo.getCurrentDate().dayOfMonth
        }?.timings ?: _state.value.timings
        val prayerTime = listOf(
            parseTimeString(stringProvider.getString(R.string.sunrise), timings.sunrise),
            parseTimeString(stringProvider.getString(R.string.fajr), timings.fajr),
            parseTimeString(stringProvider.getString(R.string.dhuhr), timings.dhuhr),
            parseTimeString(stringProvider.getString(R.string.asr), timings.asr),
            parseTimeString(stringProvider.getString(R.string.maghrib), timings.maghrib),
            parseTimeString(stringProvider.getString(R.string.isha), timings.isha),
            parseTimeString(stringProvider.getString(R.string.jumah), _state.value.jummaTiming)
        )

        viewModelScope.launch {
            settingsUtility.apply {

                prayerTime.forEach { prayerTime ->
                    setPrayerTime(prayerTime.name, prayerTime.toTimeString())
                }
                mosqueInfo.thumbnail?.let { setBackgroundImage(it) }
            }

            _effect.send(MainScreenEffect.ShowToast("Time updated from server"))
        }

        Log.d(TAG, "updated Setting with synced data: ${mosqueInfo}")

    }


    private fun setupFlows() {
        // Collect current time
        viewModelScope.launch {
            timeRepo.timeFlow.collect { time ->
                if (time.notifications.isNotEmpty()) {
                    val notification = time.notifications.first()
                    val prayerNotification = MainNotification(
                        type = NotificationType.PRAYER_TIME,
                        prayerTime = notification.prayerTime,
                        title = notification.prayerTime.name,
                        message = notification.message,
                        id = System.currentTimeMillis().toString(),
                        timestamp = System.currentTimeMillis()
                    )
                    onEvent(MainScreenEvent.AddNotification(prayerNotification))
                }
                if (_state.value.weekday != time.currentDateTime.dayOfWeek.name) {
                    onEvent(MainScreenEvent.ShowSnackBar("A new day appear, ${time.currentDateTime.dayOfWeek.name}"))
                }
                _state.update {
                    it.copy(
                        now = time.currentDateTime.toOldLocalTime(),
                        enDate = time.currentDateTime.toFormattedDate(),
                        hijriDate = timeRepo.getCurrentHijriDate().toFormattedHijriDate(),
                        weekday = time.currentDateTime.dayOfWeek.name
                    )
                }

                Log.d(
                    "TIMETESTTAG", "setupFlows: " +
                            "\nTime ${time.currentDateTime}" +
                            "\nNotifications ${time.notifications}"
                )
            }
        }
    }

    fun updatePrayerTimings(timings: Timings) {
        val prayerTime = listOf(
            parseTimeString(stringProvider.getString(R.string.sunrise), timings.sunrise),
            parseTimeString(stringProvider.getString(R.string.fajr), timings.fajr),
            parseTimeString(stringProvider.getString(R.string.dhuhr), timings.dhuhr),
            parseTimeString(stringProvider.getString(R.string.asr), timings.asr),
            parseTimeString(stringProvider.getString(R.string.maghrib), timings.maghrib),
            parseTimeString(stringProvider.getString(R.string.isha), timings.isha),
            parseTimeString(stringProvider.getString(R.string.jumah), _state.value.jummaTiming)
        )
        timeRepo.initialize(prayerTime)
    }

    private fun parseTimeString(name: String, timeString: String): PrayerTime {
        val parts = timeString.split(":")
        return PrayerTime(
            name = name,
            hour = parts[0].toInt(),
            minute = parts[1].toInt()
        )
    }


    fun clearNavigationTrigger() {
        _state.update { it.copy(isNavigationTriggered = false) }
    }
}