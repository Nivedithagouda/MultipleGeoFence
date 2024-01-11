package com.nivi.multiplegeofence.ui

import android.content.ContentValues.TAG
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.DirectionsApi
import com.google.maps.GeoApiContext
import com.google.maps.android.PolyUtil
import com.google.maps.model.DirectionsResult
import com.google.maps.model.TravelMode
import com.google.maps.model.Unit
import com.nivi.multiplegeofence.R
import com.nivi.multiplegeofence.data.model.LatLngWithCustomer
import java.util.Locale

class RouteMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private lateinit var btnAddress: ImageView
    val pointsWithCustomers = listOf(
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_route_map, container, false)

        mapView = rootView.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        btnAddress = rootView.findViewById(R.id.btnShowCustomerInfo)
        btnAddress.setOnClickListener {
            showCustomerInfoBottomSheet()
        }


        return rootView
    }

    private fun showCustomerInfoBottomSheet() {
        val bottomSheetFragment = CustomerInfoBottomSheetFragment(pointsWithCustomers)
        bottomSheetFragment.show(
            requireActivity().supportFragmentManager,
            bottomSheetFragment.tag
        )
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Enable user's location on the map
        googleMap.isMyLocationEnabled = true

        // Get the last known location and move the camera to that location
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    // Create a LatLng object for the current location
                    val currentLatLng = LatLng(location.latitude, location.longitude)

                    // Move the camera to the current location
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure to get the last known location
                Log.e(TAG, "Error getting last known location: ${exception.message}")
            }

        showRoute()
    }


    private fun showRoute() {
        // For simplicity, clear markers and polylines each time the button is clicked
//        googleMap.clear()

        // Add markers for all points with customer names as title
        for ((index, data) in pointsWithCustomers.withIndex()) {
            val address = getAddressDetails(data.latLng.latitude, data.latLng.longitude)
            googleMap.addMarker(
                MarkerOptions().position(data.latLng).title(data.customerName).snippet(address)
            )
        }

        getDirections(pointsWithCustomers)

        focusCameraOnRouteDirection(pointsWithCustomers)
    }

    private fun focusCameraOnRouteDirection(pointsWithCustomers: List<LatLngWithCustomer>) {
        if (pointsWithCustomers.size >= 2) {
            val origin = pointsWithCustomers.first().latLng
            val destination = pointsWithCustomers.last().latLng

            // Calculate the midpoint between origin and destination
            val midpoint = LatLng(
                (origin.latitude + destination.latitude) / 2,
                (origin.longitude + destination.longitude) / 2
            )

            // Move the camera to the midpoint
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(midpoint))
        }
    }

    private fun getAddressDetails(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        return if (addresses!!.isNotEmpty()) {
            val sublocality = addresses[0].subLocality ?: "Unknown Sublocality"
            val locality = addresses[0].locality ?: "Unknown Locality"
            "$sublocality, $locality"
        } else {
            "Unknown Address"
        }
    }

    private fun getDirections(pointsWithCustomers: List<LatLngWithCustomer>) {
        if (pointsWithCustomers.size < 2) {
            // At least two points are required for directions
            return
        }

        val waypoints = pointsWithCustomers.subList(1, pointsWithCustomers.size - 1)
            .map { "${it.latLng.latitude},${it.latLng.longitude}" }
            .toTypedArray()

        val origin = pointsWithCustomers.first().latLng
        val destination = pointsWithCustomers.last().latLng

        val apiKey = "AIzaSyDaWnMvfsvAN1cWaaP8rIsn3UkX7MOFVYQ" // Replace with your actual API key
        val geoApiContext = GeoApiContext.Builder().apiKey(apiKey).build()

        val directionsResult: DirectionsResult = DirectionsApi.newRequest(geoApiContext)
            .mode(TravelMode.DRIVING)
            .origin("${origin.latitude},${origin.longitude}")
            .destination("${destination.latitude},${destination.longitude}")
            .waypoints(*waypoints)
            .units(Unit.METRIC)
            .await()

        // Draw polyline for each step in the directions
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
    }

    // Handle lifecycle of the MapView
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

}
