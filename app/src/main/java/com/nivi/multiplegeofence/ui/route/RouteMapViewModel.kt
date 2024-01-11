package com.nivi.multiplegeofence.ui.route

import android.content.ContentValues.TAG
import android.graphics.Color
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.model.Unit
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import com.nivi.multiplegeofence.data.model.LatLngWithCustomer
import com.nivi.multiplegeofence.repository.GeofenceRepository

class RouteMapViewModel(private val mapRepository: GeofenceRepository) : ViewModel() {

    private val _pointsWithCustomers = MutableLiveData<List<LatLngWithCustomer>>()
    val pointsWithCustomers: LiveData<List<LatLngWithCustomer>> get() = _pointsWithCustomers

    fun fetchPointsWithCustomers() {
        _pointsWithCustomers.value = mapRepository.getPointsWithCustomers()
    }

    fun focusCameraOnRouteDirection(
        googleMap: GoogleMap,
        pointsWithCustomers: List<LatLngWithCustomer>
    ) {
        if (pointsWithCustomers.size >= 2) {
            val origin = pointsWithCustomers.first().latLng
            val destination = pointsWithCustomers.last().latLng
            val midpoint = LatLng(
                (origin.latitude + destination.latitude) / 2,
                (origin.longitude + destination.longitude) / 2
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(midpoint))
        }

        if (pointsWithCustomers.size >= 2) {
            getDirections(googleMap, pointsWithCustomers)
        }
    }

    fun getDirections(googleMap: GoogleMap, pointsWithCustomers: List<LatLngWithCustomer>) {
        if (pointsWithCustomers.size < 2) {
            return
        }

        val waypoints = pointsWithCustomers.subList(1, pointsWithCustomers.size - 1)
            .map { "${it.latLng.latitude},${it.latLng.longitude}" }
            .toTypedArray()

        val origin = pointsWithCustomers.first().latLng
        val destination = pointsWithCustomers.last().latLng

        val apiKey = "AIzaSyDaWnMvfsvAN1cWaaP8rIsn3UkX7MOFVYQ" // Replace with your actual API key
        val geoApiContext = GeoApiContext.Builder().apiKey(apiKey).build()

        try {
            val directionsResult: DirectionsResult = DirectionsApi.newRequest(geoApiContext)
                .mode(TravelMode.DRIVING)
                .origin("${origin.latitude},${origin.longitude}")
                .destination("${destination.latitude},${destination.longitude}")
                .waypoints(*waypoints)
                .units(Unit.METRIC)
                .await()

            for (route in directionsResult.routes) {
                for (leg in route.legs) {
                    for (step in leg.steps) {
                        val polylineOptions = PolylineOptions()
                            .addAll(PolyUtil.decode(step.polyline.encodedPath))
                            .color(Color.BLUE)
                            .width(5f)
                        googleMap.addPolyline(polylineOptions)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching directions: ${e.message}")
        }
    }
}

