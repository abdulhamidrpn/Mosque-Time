package com.rpn.mosquetime.di

import com.rpn.mosquetime.presentation.screen.login.LoginViewModel
import com.rpn.mosquetime.presentation.screen.main.MainViewModel
import com.rpn.mosquetime.presentation.screen.message.MessageViewModel
import com.rpn.mosquetime.presentation.screen.settings.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel( get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(), get(),get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { MessageViewModel(get(), get()) }
}
