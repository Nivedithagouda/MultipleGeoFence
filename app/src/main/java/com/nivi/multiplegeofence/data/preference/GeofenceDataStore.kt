package com.nivi.multiplegeofence.data.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "geofence_preferences")

class GeofenceDataStore(context: Context) {

    private val dataStore: DataStore<Preferences> = context.dataStore
    companion object {
        private val GEOFENCE_KEY = stringPreferencesKey("geofence_key")
    }

    suspend fun saveGeofence(lat: Double, lon: Double, radius: Double) {
        dataStore.edit { preferences ->
            val existingGeofences = preferences[GEOFENCE_KEY]?.split(";")?.toMutableList() ?: mutableListOf()
            existingGeofences.add("$lat,$lon,$radius")
            preferences[GEOFENCE_KEY] = existingGeofences.joinToString(separator = ";")
        }
    }

    fun getGeofence(): Flow<List<Triple<Double, Double, Double>>> {
        return dataStore.data.map { preferences ->
            val geofenceString = preferences[GEOFENCE_KEY]
            geofenceString?.let {
                it.split(";").mapNotNull { geoStr ->
                    val parts = geoStr.split(",")
                    if (parts.size == 3) {
                        Triple(parts[0].toDouble(), parts[1].toDouble(), parts[2].toDouble())
                    } else {
                        null
                    }
                }
            } ?: emptyList()
        }
    }

    suspend fun clearGeofence(lat: Double, lon: Double, radius: Double) {
        dataStore.edit { preferences ->
            val existingGeofences = preferences[GEOFENCE_KEY]?.split(";")?.toMutableList() ?: mutableListOf()

            existingGeofences.removeAll {
                val parts = it.split(",")
                parts.size == 3 &&
                        parts[0].toDouble() == lat &&
                        parts[1].toDouble() == lon &&
                        parts[2].toDouble() == radius
            }

            if (existingGeofences.isEmpty()) {
                preferences.remove(GEOFENCE_KEY)
            } else {
                preferences[GEOFENCE_KEY] = existingGeofences.joinToString(separator = ";")
            }
        }
    }


}