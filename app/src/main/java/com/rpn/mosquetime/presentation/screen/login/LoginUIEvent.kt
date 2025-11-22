package com.rpn.mosquetime.presentation.screen.login


sealed class LoginEvent {
    data class EmailChanged(val email: String) : LoginEvent()
    data class PasswordChanged(val password: String) : LoginEvent()
    object Submit : LoginEvent()
    object BackToHome : LoginEvent()
}
