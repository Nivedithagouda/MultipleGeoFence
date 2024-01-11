// RouteMapFragment.kt
package com.nivi.multiplegeofence.ui.route

import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
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
import com.nivi.multiplegeofence.ui.utility.getAddressDetails
import org.koin.androidx.viewmodel.ext.android.viewModel

class RouteMapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap
    private val mapViewModel: RouteMapViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_route_map, container, false)

        mapView = rootView.findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        rootView.findViewById<View>(R.id.btnShowCustomerInfo).setOnClickListener {
            showCustomerInfoBottomSheet()
        }

        return rootView
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.isMyLocationEnabled = true

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error getting last known location: ${exception.message}")
            }

        mapViewModel.pointsWithCustomers.observe(viewLifecycleOwner, Observer { points ->
            // Handle the updated data
            // Update the map or perform any other UI-related tasks
            showRoute()
        })

        mapViewModel.fetchPointsWithCustomers()
    }

    private fun showRoute() {
        googleMap.clear()

        val pointsWithCustomers = mapViewModel.pointsWithCustomers.value.orEmpty()

        for ((index, data) in pointsWithCustomers.withIndex()) {
            val address = getAddressDetails(requireContext(), data.latLng.latitude, data.latLng.longitude)
            googleMap.addMarker(
                MarkerOptions().position(data.latLng).title(data.customerName).snippet(address)
            )
        }

        focusCameraOnRouteDirection(pointsWithCustomers)
    }

    private fun focusCameraOnRouteDirection(pointsWithCustomers: List<LatLngWithCustomer>) {
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
            getDirections(pointsWithCustomers)
        }
    }

    private fun getDirections(pointsWithCustomers: List<LatLngWithCustomer>) {
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

    private fun showCustomerInfoBottomSheet() {
        val bottomSheetFragment = CustomerInfoBottomSheetFragment(mapViewModel.pointsWithCustomers.value.orEmpty())
        bottomSheetFragment.show(
            requireActivity().supportFragmentManager,
            bottomSheetFragment.tag
        )
    }

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
