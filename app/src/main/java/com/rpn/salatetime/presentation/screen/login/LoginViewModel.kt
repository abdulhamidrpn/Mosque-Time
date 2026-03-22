package com.rpn.salatetime.presentation.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rpn.salatetime.domain.repository.AuthRepository
import com.rpn.salatetime.utils.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect: SharedFlow<LoginEffect> = _effect.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> _state.update { it.copy(email = event.email) }
            is LoginEvent.PasswordChanged -> _state.update { it.copy(password = event.password) }
            is LoginEvent.TogglePasswordVisibility ->
                _state.update { it.copy(showPassword = !it.showPassword) }

            is LoginEvent.Submit -> login()
        }
    }

    private fun login() {
        val current = _state.value

        // ── Client-side validation ────────────────────────────────────────────
        if (current.email.isBlank()) {
            emitSnackbar("Please enter your email address.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(current.email.trim()).matches()) {
            emitSnackbar("Please enter a valid email address.")
            return
        }
        if (current.password.isBlank()) {
            emitSnackbar("Please enter your password.")
            return
        }
        if (current.password.length < 6) {
            emitSnackbar("Password must be at least 6 characters.")
            return
        }

        viewModelScope.launch {
            authRepository.login(
                email = current.email.trim(),
                password = current.password
            ).collect { result ->
                when (result) {
                    is Result.Loading -> _state.update { it.copy(isLoading = true) }

                    is Result.Success -> {
                        _state.update { it.copy(isLoading = false, user = result.data) }
                        _effect.emit(LoginEffect.NavigateToHome)
                    }

                    is Result.Error -> {
                        _state.update { it.copy(isLoading = false) }
                        emitSnackbar(result.message ?: "Login failed. Please try again.")
                    }
                }
            }
        }
    }

    private fun emitSnackbar(message: String) {
        viewModelScope.launch { _effect.emit(LoginEffect.ShowSnackbar(message)) }
    }
}