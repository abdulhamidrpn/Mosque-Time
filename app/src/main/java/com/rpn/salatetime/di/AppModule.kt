package com.rpn.salatetime.di

import com.rpn.salatetime.data.local.db.MosqueDatabase
import com.rpn.salatetime.ui.theme.AppStrings
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module


val appModule = module {
    // Database
    single { MosqueDatabase.getDatabase(androidContext()) }
    single { get<MosqueDatabase>().mosqueDao() }


    single { AppStrings(context = androidContext()) }
}
