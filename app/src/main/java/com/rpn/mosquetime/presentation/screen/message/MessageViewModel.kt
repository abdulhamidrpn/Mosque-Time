package com.rpn.mosquetime.presentation.screen.message

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rpn.mosquetime.R
import com.rpn.mosquetime.domain.usecase.GetLatestMessageUseCase
import com.rpn.mosquetime.utils.AppStringProvider
import com.rpn.mosquetime.utils.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MessageViewModel(
    private val getLatestMessageUseCase: GetLatestMessageUseCase,
    private val stringProvider: AppStringProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(MessageUIState())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<MessageUIEffect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        loadSampleNotifications()
    }

    private fun loadSampleNotifications() {
        val sampleNotifications = listOf(
            NotificationItem(
                id = UUID.randomUUID().toString(),
                title = stringProvider.getString(R.string.prayer_time),
                message = "Fajr prayer time is approaching in 15 minutes",
                timestamp = System.currentTimeMillis() - 300000, // 5 minutes ago
                type = NotificationType.PRAYER_TIME
            ),
            NotificationItem(
                id = UUID.randomUUID().toString(),
                title = stringProvider.getString(R.string.new_message),
                message = "Welcome to the mosque! Please maintain silence during prayers.",
                timestamp = System.currentTimeMillis() - 600000, // 10 minutes ago
                type = NotificationType.MESSAGE
            ),
            NotificationItem(
                id = UUID.randomUUID().toString(),
                title = stringProvider.getString(R.string.system_update),
                message = "Prayer times have been updated for today",
                timestamp = System.currentTimeMillis() - 900000, // 15 minutes ago
                type = NotificationType.SYSTEM
            )
        )
        _uiState.value = _uiState.value.copy(notifications = sampleNotifications)
    }

    fun onEvent(event: MessageUIEvent) {
        when (event) {
            MessageUIEvent.LoadMessage -> {
                viewModelScope.launch {
                    getLatestMessageUseCase("86ohJRfbniiVd3vapwHL").collect {
                        when (it) {
                            is Result.Loading -> {
                                _uiState.value = _uiState.value.copy(isLoading = true)
                            }
                            is Result.Success -> {
                                _uiState.value = _uiState.value.copy(isLoading = false, message = it.data ?: "")
                                _uiEffect.emit(MessageUIEffect.ShowToast(stringProvider.getString(R.string.message_loaded)))
                            }
                            is Result.Error -> {
                                _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
                                _uiEffect.emit(MessageUIEffect.ShowToast(it.message ?: stringProvider.getString(R.string.unknown_error)))
                            }
                        }
                    }
                }
            }
            MessageUIEvent.ToggleNotifications -> {
                _uiState.value = _uiState.value.copy(
                    showNotifications = !_uiState.value.showNotifications
                )
            }
            MessageUIEvent.ClearNotifications -> {
                _uiState.value = _uiState.value.copy(notifications = emptyList())
                viewModelScope.launch {
                    _uiEffect.emit(MessageUIEffect.ShowToast(stringProvider.getString(R.string.all_notifications_cleared)))
                }
            }
            is MessageUIEvent.DismissNotification -> {
                val updatedNotifications = _uiState.value.notifications.filter { it.id != event.id }
                _uiState.value = _uiState.value.copy(notifications = updatedNotifications)
                viewModelScope.launch {
                    _uiEffect.emit(MessageUIEffect.ShowToast(stringProvider.getString(R.string.notification_dismissed)))
                }
            }
        }
    }
}