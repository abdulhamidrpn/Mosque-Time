package com.rpn.salatetime.presentation.screen.main

import com.rpn.salatetime.domain.model.NotificationTrigger


sealed class MainScreenEffect {
    data class ShowToast(val message: String) : MainScreenEffect()
    data class Error(val message: String) : MainScreenEffect()
    data object NavigateToSettings : MainScreenEffect()

    data object CloseMessageScreen : MainScreenEffect()
    data class NavigateToMessage(val notification: NotificationTrigger) : MainScreenEffect()
}
