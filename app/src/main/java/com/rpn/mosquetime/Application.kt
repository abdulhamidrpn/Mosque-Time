package com.rpn.mosquetime

import android.app.Application
import com.rpn.mosquetime.di.appModule
import com.rpn.mosquetime.di.imageModule
import com.rpn.mosquetime.di.mainRepositoryModule
import com.rpn.mosquetime.di.useCaseModule
import com.rpn.mosquetime.di.utilsModule
import com.rpn.mosquetime.di.viewModelModule
import com.rpn.mosquetime.utils.NotificationUtil
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class Application : Application() {


    override fun onCreate() {
        super.onCreate()

        val modules =
            listOf(appModule, mainRepositoryModule, useCaseModule, viewModelModule, utilsModule,
                imageModule
            )
        startKoin {
            androidContext(this@Application)
            modules(modules)
        }
        NotificationUtil.createNotificationChannel(this)
    }

    companion object {
        const val CHANNEL_ID = "ALARM_SERVICE_CHANNEL"
    }
}

