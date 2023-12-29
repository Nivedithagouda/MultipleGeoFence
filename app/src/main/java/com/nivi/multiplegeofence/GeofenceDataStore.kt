package com.nivi.multiplegeofence

// GeofenceDataStore.kt
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



    suspend fun saveGeofence(lat: Double, lon: Double) {
        dataStore.edit { preferences ->
            val existingGeofences = preferences[GEOFENCE_KEY]?.split(";")?.toMutableList() ?: mutableListOf()
            existingGeofences.add("$lat,$lon")
            preferences[GEOFENCE_KEY] = existingGeofences.joinToString(separator = ";")
        }
    }


    fun getGeofence(): Flow<List<Pair<Double, Double>>> {
        return dataStore.data.map { preferences ->
            val geofenceString = preferences[GEOFENCE_KEY]
            geofenceString?.let {
                it.split(";").mapNotNull { geoStr ->
                    val parts = geoStr.split(",")
                    if (parts.size == 2) {
                        Pair(parts[0].toDouble(), parts[1].toDouble())
                    } else {
                        null
                    }
                }
            } ?: emptyList()
        }
    }

    suspend fun clearGeofence(lat: Double, lon: Double) {
        dataStore.edit { preferences ->
            val updatedGeofences = preferences[GEOFENCE_KEY]?.split(";")
                ?.filter { it.isNotEmpty() }
                ?.filterNot {
                    val parts = it.split(",")
                    parts.size == 2 && parts[0].toDouble() == lat && parts[1].toDouble() == lon
                }
                ?.joinToString(separator = ";")

            updatedGeofences?.let {
                preferences[GEOFENCE_KEY] = it
            } ?: preferences.remove(GEOFENCE_KEY)
        }
    }
    // GeofenceDataStore.kt

    fun getGeofences(): Flow<List<Pair<Double, Double>>> {
        return dataStore.data.map { preferences ->
            val geofenceString = preferences[GEOFENCE_KEY]
            geofenceString?.let {
                it.split(";").mapNotNull { geoStr ->
                    val parts = geoStr.split(",")
                    if (parts.size == 2) {
                        Pair(parts[0].toDouble(), parts[1].toDouble())
                    } else {
                        null
                    }
                }
            } ?: emptyList()
        }
    }

}

