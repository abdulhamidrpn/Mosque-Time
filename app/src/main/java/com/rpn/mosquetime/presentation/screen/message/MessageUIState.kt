package com.rpn.mosquetime.presentation.screen.message

data class MessageUIState(
    val message: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val notifications: List<NotificationItem> = emptyList(),
    val showNotifications: Boolean = false
)

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: NotificationType
)

enum class NotificationType {
    PRAYER_TIME,
    MESSAGE,
    SYSTEM
}