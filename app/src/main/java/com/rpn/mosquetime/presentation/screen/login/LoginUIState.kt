package com.rpn.mosquetime.presentation.screen.login

import com.google.firebase.auth.FirebaseUser

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val error: String? = null
)
