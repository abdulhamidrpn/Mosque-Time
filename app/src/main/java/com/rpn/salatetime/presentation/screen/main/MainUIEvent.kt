package com.rpn.salatetime.presentation.screen.main

import com.rpn.salatetime.domain.model.NotificationTrigger


sealed class MainScreenEvent {

    data class LoadMosqueData(val ownerUid: String) : MainScreenEvent()
    data object OnMoreClick : MainScreenEvent()
    data object OnQrClick : MainScreenEvent()
    data object Tick : MainScreenEvent() // internal clock tick
    data object DismissNotificationBanner : MainScreenEvent()
    data class AddNotification(val notification: NotificationTrigger) : MainScreenEvent()
    data class RemoveNotification(val id: String) : MainScreenEvent()

    data class ShowSnackBar(val message: String) : MainScreenEvent()
}
