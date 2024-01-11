// GeofenceRepository.kt
package com.nivi.multiplegeofence.repository

import android.content.Context
import com.nivi.multiplegeofence.data.preference.GeofenceDataStore
import kotlinx.coroutines.flow.Flow

class GeofenceRepository(private val dataStore: GeofenceDataStore) {

    suspend fun saveGeofence(lat: Double, lon: Double, radius: Double) {
        dataStore.saveGeofence(lat, lon, radius)
    }

    fun getGeofenceList(): Flow<List<Triple<Double, Double, Double>>> {
        return dataStore.getGeofence()
    }

    suspend fun clearGeofence(lat: Double, lon: Double, radius: Double) {
        dataStore.clearGeofence(lat, lon, radius)
    }
}
