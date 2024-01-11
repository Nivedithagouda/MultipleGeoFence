package com.nivi.multiplegeofence.repository

import com.google.android.gms.maps.model.LatLng
import com.nivi.multiplegeofence.data.model.LatLngWithCustomer
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

    private val pointsWithCustomers = listOf(
        LatLngWithCustomer("Customer 1", LatLng(12.9514, 77.6518)),
        LatLngWithCustomer("Customer 2", LatLng(12.9279, 77.6271)),
        LatLngWithCustomer("Customer 3", LatLng(12.9716, 77.6412)),
        LatLngWithCustomer("Customer 4", LatLng(12.9270, 77.6742)),
        LatLngWithCustomer("Customer 5", LatLng(12.9200, 77.6206)),
        LatLngWithCustomer("Customer 6", LatLng(12.9569, 77.7011)),
        LatLngWithCustomer("Customer 7", LatLng(12.9577, 77.5978)),
        LatLngWithCustomer("Customer 8", LatLng(13.0104, 77.6518)),
        LatLngWithCustomer("Customer 9", LatLng(12.9275, 77.5907)),
    )

    fun getPointsWithCustomers(): List<LatLngWithCustomer> {
        return pointsWithCustomers
    }
}
