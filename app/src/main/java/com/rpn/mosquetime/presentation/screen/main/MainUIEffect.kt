package com.rpn.mosquetime.presentation.screen.main


sealed class MainScreenEffect {
    data class ShowToast(val message: String) : MainScreenEffect()
    data class ShowImageMessage(val image: String) : MainScreenEffect()
    data object OpenMore : MainScreenEffect()
    data object OpenQr : MainScreenEffect()
    data class Error(val message: String) : MainScreenEffect()
    data class ShowNotification(val notification: MainNotification) : MainScreenEffect()
}
