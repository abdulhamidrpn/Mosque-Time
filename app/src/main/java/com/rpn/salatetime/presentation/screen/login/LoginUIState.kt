package com.rpn.salatetime.presentation.screen.login

import io.github.jan.supabase.auth.user.UserInfo


data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val showPassword: Boolean = false,
    val user: UserInfo? = null,
)