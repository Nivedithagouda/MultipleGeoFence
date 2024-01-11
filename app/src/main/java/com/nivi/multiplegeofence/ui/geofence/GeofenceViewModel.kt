// GeofenceViewModel.kt
package com.nivi.multiplegeofence.ui.geofence

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nivi.multiplegeofence.repository.GeofenceRepository
import com.nivi.multiplegeofence.ui.utility.getAddressDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GeofenceViewModel(private val repository: GeofenceRepository) : ViewModel() {

    val geofenceList: Flow<List<Triple<Double, Double, Double>>> = repository.getGeofenceList()

    fun saveGeofence(lat: Double, lon: Double, radius: Double) {
        viewModelScope.launch {
            repository.saveGeofence(lat, lon, radius)
        }
    }

    fun clearGeofence(lat: Double, lon: Double, radius: Double) {
        viewModelScope.launch {
            repository.clearGeofence(lat, lon, radius)
        }
    }

     fun getAddress(context: Context, latitude: Double, longitude: Double): String {
       return getAddressDetails(context,latitude,longitude)
    }

}
