package com.nivi.multiplegeofence.ui

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.nivi.multiplegeofence.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin


class BaseClass : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BaseClass)
            Places.initialize(applicationContext, "AIzaSyBBach7hwq7Y24aFo9j4SUWy92vAv1-f2E")
            modules(appModule)
        }
    }
}
