package com.rpn.mosquetime.ui.viewmodel

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val viewModelModule = module {

    single { MainViewModel() }
    viewModel { LoginRegisterViewModel(get (),get()) }
}
// AlbumViewModel by inject { parametersOf(a, b) }
