package com.rpn.mosquetime.presentation.screen.message

sealed class MessageUIEvent {
    object LoadMessage : MessageUIEvent()
    object ToggleNotifications : MessageUIEvent()
    object ClearNotifications : MessageUIEvent()
    data class DismissNotification(val id: String) : MessageUIEvent()
}