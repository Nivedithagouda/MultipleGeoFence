package com.nivi.multiplegeofence.ui.utility

import android.content.Context
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




