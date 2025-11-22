package com.rpn.mosquetime.presentation.screen.message

sealed class MessageUIEffect {
    data class ShowToast(val message: String) : MessageUIEffect()
    data object NavigateToHome : MessageUIEffect()
}