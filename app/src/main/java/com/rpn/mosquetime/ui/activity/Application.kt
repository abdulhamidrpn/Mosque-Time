package com.rpn.mosquetime.ui.activity

import android.app.Application
import com.rpn.mosquetime.repository.di.mainRepositoryModule
import com.rpn.mosquetime.ui.viewmodel.viewModelModule
import com.rpn.mosquetime.utils.utilsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Application : Application() {


    override fun onCreate() {
        super.onCreate()

        val modules = listOf(
            mainRepositoryModule,
            viewModelModule,
            utilsModule
        )
        startKoin {
            androidContext(this@Application)
            modules(modules)
        }
    }

    companion object {
        const val CHANNEL_ID = "ALARM_SERVICE_CHANNEL"
    }
}

