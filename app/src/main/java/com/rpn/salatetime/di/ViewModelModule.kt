package com.rpn.salatetime.di

import com.rpn.salatetime.presentation.screen.login.LoginViewModel
import com.rpn.salatetime.presentation.screen.main.MainViewModel
import com.rpn.salatetime.presentation.screen.setting.SettingsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get(), get(), get(), get()) }
    viewModel { SettingsViewModel(get(),get(),get(), get()) }
//    viewModel { MainViewModel(get(),get(), get(), get(), get(), get(), get(), get(), get()) }
//    viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { LoginViewModel( get()) }
}
