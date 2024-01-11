package com.nivi.multiplegeofence.di

import com.nivi.multiplegeofence.data.preference.GeofenceDataStore
import com.nivi.multiplegeofence.repository.GeofenceRepository
import com.nivi.multiplegeofence.ui.geofence.GeofenceViewModel
import com.nivi.multiplegeofence.ui.route.RouteMapViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val appModule: Module = module {
    single { GeofenceDataStore(get()) }
    single { GeofenceRepository(get()) }
    viewModel { GeofenceViewModel(get()) }
    viewModel { RouteMapViewModel(get()) }
}
