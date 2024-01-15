package com.nivi.multiplegeofence.ui.utility

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

object LocationUtility {

    interface LocationResultListener {
        fun onLocationResult(location: Location)
        fun onLocationError(error: String)
    }

    fun getCurrentLocation(context: Context, listener: LocationResultListener) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // Check if the necessary permissions are granted
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Create location request
            val locationRequest = LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 10000 // 10 seconds
            }

            // Create location callback
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    val lastLocation: Location? = locationResult.lastLocation
                    if (lastLocation != null) {
                        listener.onLocationResult(lastLocation)
                    } else {
                        listener.onLocationError("Unable to get location")
                    }
                }
            }

            // Request location updates
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } else {
            // Permission not granted, inform the listener
            listener.onLocationError("Location permission not granted")
        }
    }
}
