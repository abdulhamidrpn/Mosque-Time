package com.rpn.mosquetime.utils

import org.koin.dsl.module

val utilsModule = module {
    factory { SettingsUtility(get()) }
}