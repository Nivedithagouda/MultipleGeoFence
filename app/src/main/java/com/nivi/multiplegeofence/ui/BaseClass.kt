package com.nivi.multiplegeofence.ui

import android.app.Application
import com.nivi.multiplegeofence.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin


class BaseClass : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BaseClass)
            modules(appModule)
        }
    }
}
