// GeofenceModule.kt
package com.nivi.multiplegeofence.di

import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nivi.multiplegeofence.data.preference.GeofenceDataStore
import com.nivi.multiplegeofence.repository.GeofenceRepository
import com.nivi.multiplegeofence.ui.GeofenceViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
    single { GeofenceDataStore(get()) }
    single { GeofenceRepository(get()) }
    viewModel { GeofenceViewModel(get()) }
}
