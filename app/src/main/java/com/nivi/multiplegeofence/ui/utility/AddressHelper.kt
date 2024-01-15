package com.nivi.multiplegeofence.ui.utility

import android.content.Context
import android.location.Address
import android.location.Geocoder
import java.util.*

fun getAddressDetails(context: Context, latitude: Double, longitude: Double): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        if (addresses?.isNotEmpty() == true) {
            val sublocality = addresses[0].subLocality ?: "Unknown Sublocality"
            val locality = addresses[0].locality ?: "Unknown Locality"
            "$sublocality, $locality"
        } else {
            "Unknown Address"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Error obtaining address"
    }

}

fun getLatLngFromAddress(context: Context, address: String): Pair<Double, Double>? {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address>? = geocoder.getFromLocationName(address, 1)

        if (addresses != null && addresses.isNotEmpty()) {
            val latitude = addresses[0].latitude
            val longitude = addresses[0].longitude
            Pair(latitude, longitude)
        } else {
            null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}





