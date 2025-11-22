package com.rpn.mosquetime.presentation.screen.main


sealed class MainScreenEvent {
    object Refresh : MainScreenEvent()
    data object OnMoreClick : MainScreenEvent()
    data object OnQrClick : MainScreenEvent()
    data object Tick : MainScreenEvent() // internal clock tick
    data object DismissNotificationBanner : MainScreenEvent()
    data class AddNotification(val notification: MainNotification) : MainScreenEvent()
    data class RemoveNotification(val id: String) : MainScreenEvent()

    data class ShowSnackBar(val message: String) : MainScreenEvent()
}
