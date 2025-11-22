package com.rpn.mosquetime.di

import com.rpn.mosquetime.utils.AppStringProvider
import org.koin.dsl.module

val utilsModule = module {
    factory { AppStringProvider(get()) }
}