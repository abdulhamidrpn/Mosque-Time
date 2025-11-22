package com.rpn.mosquetime.presentation.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rpn.mosquetime.R
import com.rpn.mosquetime.domain.usecase.LoginUserUseCase
import com.rpn.mosquetime.utils.AppStringProvider
import com.rpn.mosquetime.utils.Result
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val loginUserUseCase: LoginUserUseCase,
    private val stringProvider: AppStringProvider
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _effect = MutableSharedFlow<LoginEffect>()
    val effect = _effect.asSharedFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged -> {
                _state.update { it.copy(email = event.email) }
            }

            is LoginEvent.PasswordChanged -> {
                _state.update { it.copy(password = event.password) }
            }

            LoginEvent.Submit -> {
                login()
            }

            LoginEvent.BackToHome -> {
                viewModelScope.launch {
                    _effect.emit(LoginEffect.NavigateToHome)
                }
            }
        }
    }

    private fun login() {
        viewModelScope.launch {
            loginUserUseCase(
                _state.value.email,
                _state.value.password
            ).collect { result ->
                when (result) {
                    is Result.Loading -> {
                        _state.update { it.copy(isLoading = true) }
                    }

                    is Result.Success -> {
                        _state.update { it.copy(isLoading = false, user = result.data) }
                        _effect.emit(LoginEffect.NavigateToHome)
                    }

                    is Result.Error -> {
                        _state.update { it.copy(isLoading = false, error = result.message) }
                        _effect.emit(LoginEffect.ShowError(result.message ?: stringProvider.getString(R.string.unknown_error)))
                    }
                }
            }
        }
    }
}
