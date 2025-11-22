package com.rpn.mosquetime.presentation.screen.login


sealed class LoginEffect {
    object NavigateToHome : LoginEffect()
    data class ShowError(val message: String) : LoginEffect()
}
