package com.nivi.multiplegeofence.ui.route

import android.content.ContentValues.TAG
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
        val pointsWithCustomers = mapViewModel.pointsWithCustomers.value.orEmpty()

        for ((index, data) in pointsWithCustomers.withIndex()) {
            val address =
                getAddressDetails(requireContext(), data.latLng.latitude, data.latLng.longitude)
            googleMap.addMarker(
                MarkerOptions().position(data.latLng).title(data.customerName).snippet(address)
            )
        }

        focusCameraOnRouteDirection(pointsWithCustomers)
        getDirections(pointsWithCustomers)
    }


    private fun showCustomerInfoBottomSheet() {
        val bottomSheetFragment =
            CustomerInfoBottomSheetFragment(mapViewModel.pointsWithCustomers.value.orEmpty())
        bottomSheetFragment.show(
            requireActivity().supportFragmentManager,
            bottomSheetFragment.tag
        )
    }

    private fun focusCameraOnRouteDirection(pointsWithCustomers: List<LatLngWithCustomer>) {
        mapViewModel.focusCameraOnRouteDirection(googleMap, pointsWithCustomers)
    }

    private fun getDirections(pointsWithCustomers: List<LatLngWithCustomer>) {
        mapViewModel.getDirections(googleMap, pointsWithCustomers)
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
