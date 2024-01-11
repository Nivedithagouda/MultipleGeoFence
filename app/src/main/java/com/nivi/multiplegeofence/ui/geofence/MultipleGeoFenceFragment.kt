package com.nivi.multiplegeofence.ui.geofence

import android.Manifest
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.nivi.multiplegeofence.R
import com.nivi.multiplegeofence.data.model.GeofenceItem
import com.nivi.multiplegeofence.data.preference.GeofenceDataStore
import com.nivi.multiplegeofence.data.receiver.GeofenceBroadcastReceiver
import com.nivi.multiplegeofence.data.service.ForegroundService
import com.nivi.multiplegeofence.ui.route.CustomerInfoBottomSheetFragment
import com.nivi.multiplegeofence.ui.utility.PermissionManager
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


class MultipleGeoFenceFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var recyclerView: RecyclerView

    private val GEOFENCE_RADIUS = 100.0f
    private var isMapInitialized = false
    private val GEOFENCE_REQ_CODE = 0
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    private val geofenceList = mutableListOf<GeofenceItem>()
    private val geofenceAdapter = GeofenceAdapter(geofenceList)
    private val geofenceViewModel: GeofenceViewModel by viewModel()
    private lateinit var geofenceDataStore: GeofenceDataStore

    val geofenceCheckHandler = Handler(Looper.getMainLooper())
    private val geofenceCheckRunnable = object : Runnable {
        override fun run() {
            startGeofence()
            geofenceCheckHandler.postDelayed(this, 3600000) // Schedule next check after 30 minute
//            geofenceCheckHandler.postDelayed(this, 300000)// Schedule next check after 5 minute
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_multiple_geo_fence, container, false)

        // Initialize geofenceDataStore
        geofenceDataStore = GeofenceDataStore(requireContext())



        val mapFragment =
            childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
        view.findViewById<View>(R.id.btnShowCustomerInfo).setOnClickListener {
            showCustomerInfoBottomSheet()
        }

        return view
    }

    private fun loadGeofenceFromDataStore() {
        lifecycleScope.launch {
            geofenceViewModel.geofenceList.collect { geofenceList ->
                geofenceList.let {
                    for ((latitude, longitude, radius) in it) {
                        val latLng = LatLng(latitude, longitude)
                        createRadiusInputDialog(latLng, radius)
                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMapLongClickListener { latLng ->
            createRadiusInputDialog(latLng, null)
        }
        checkLocationPermission()
        loadGeofenceFromDataStore()
        startGeofence()

        val foregroundServiceIntent = Intent(requireContext(), ForegroundService::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            requireActivity().startForegroundService(foregroundServiceIntent)
        } else {
            requireActivity().startService(foregroundServiceIntent)
        }
    }

    override fun onResume() {
        super.onResume()
        initializeMapIfNeeded()
    }

    private fun initializeMapIfNeeded() {
        if (checkPermission() && !isMapInitialized) {
            val mapFragment =
                childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            mapFragment.getMapAsync(this)
            isMapInitialized = true
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            ) {
                // Show an explanation to the user and then request the permission again
                showPermissionExplanationDialog()
            } else {
                // No explanation needed; request the permission
                requestLocationPermission()
            }
        } else {
            map.isMyLocationEnabled = true
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            permissions,
            GEOFENCE_REQ_CODE
        )
    }
    private fun showPermissionExplanationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Location Permission Required")
            .setMessage("This app requires location permission to function properly.")
            .setPositiveButton("OK") { _, _ ->PermissionManager.openAppSettings(requireContext()) }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    private fun addGeofenceMarker(latLng: LatLng, radius: Double) {
        // Check if the geofence with the same LatLng already exists
        if (geofenceList.any { it.marker.position == latLng }) {
            // Geofence with the same LatLng already exists, do not add again
            return
        }
        val address= geofenceViewModel.getAddress(requireContext(),latLng.latitude,latLng.longitude)
        val markerOptions = MarkerOptions()
            .position(latLng)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))

            .title("$address ${geofenceList.size}")
            .draggable(true)
        val circleOptions = CircleOptions()
            .center(latLng)
            .radius(radius)
            .strokeColor(ContextCompat.getColor(requireContext(), R.color.black))
            .fillColor(Color.GRAY)

        val circle = map.addCircle(circleOptions)
        val marker = map.addMarker(markerOptions)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        var geofenceItem: GeofenceItem? = null

        marker?.let {
            geofenceItem = GeofenceItem(it, circle) {
                // Handle delete action
                geofenceItem?.let { it1 -> deleteGeofence(it1, latLng,radius) }
            }
        }
        geofenceViewModel.saveGeofence(latLng.latitude,latLng.longitude,radius)

        geofenceItem?.let {
            geofenceList.add(it)
            geofenceAdapter.notifyDataSetChanged()
        }
    }



    private fun startGeofence() {
        if (geofenceList.isNotEmpty()) {
            for (i in geofenceList.indices) {
                val geofence = createGeofence(geofenceList[i])
                val geofenceRequest = createGeofenceRequest(geofence)
                addGeofence(geofenceRequest)
            }
        }
    }

    private fun createGeofence(geofenceItem: GeofenceItem): Geofence {
        return Geofence.Builder()
            .setRequestId(geofenceItem.marker.title.toString())
            .setCircularRegion(
                geofenceItem.marker.position.latitude,
                geofenceItem.marker.position.longitude,
                GEOFENCE_RADIUS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()
    }


    private fun createGeofenceRequest(geofence: Geofence): GeofencingRequest {
        return GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()
    }

    private fun addGeofence(request: GeofencingRequest) {
        val pendingIntent = getGeofencePendingIntent()

        val geofencingClient = LocationServices.getGeofencingClient(requireContext())
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient.addGeofences(request, pendingIntent)?.run {
                addOnSuccessListener {
//                    Toast.makeText(this@MainActivity, "Geofence added", Toast.LENGTH_SHORT).show()
                }
                addOnFailureListener { exception ->
                    Toast.makeText(
                        requireContext(),
                        "Geofence failed to add: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            requireContext(),
            GEOFENCE_REQ_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == GEOFENCE_REQ_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                if (checkPermission()) {
                    map.isMyLocationEnabled = true
                } else {
                    // Handle the case where permission is not available even after request
                    Toast.makeText(
                        requireContext(),
                        "Location permission not available. Geofencing will not work.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Permission denied, show a message or navigate to app settings
                showPermissionDeniedMessageOrNavigateToSettings()
            }
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }


    private fun deleteGeofence(geofenceItem: GeofenceItem, latLng: LatLng, radius: Double) {
        // Remove geofence from the map
        geofenceItem.marker.remove()
        geofenceItem.circle.remove()

        // Remove geofence from the list
        geofenceList.remove(geofenceItem)
        geofenceViewModel.clearGeofence(latLng.latitude,latLng.longitude,radius)

        // Notify the adapter that the data set has changed
        geofenceAdapter.notifyDataSetChanged()
    }



    private fun createRadiusInputDialog(latLng: LatLng, savedRadius: Double?) {
        if (savedRadius != null) {
            // Radius is already available, add geofence directly
            addGeofenceMarker(latLng, savedRadius)
        } else {
            // Radius is not available, show the input dialog
            val builder = AlertDialog.Builder(requireContext())
            val inputRadius = EditText(requireContext())
            inputRadius.inputType = InputType.TYPE_CLASS_NUMBER
            builder.setTitle("Enter Geofence Radius")
                .setMessage("Enter the radius for the geofence in meters:")
                .setView(inputRadius)
                .setPositiveButton("OK") { dialog, _ ->
                    val radius = inputRadius.text.toString().toDoubleOrNull()
                    if (radius != null) {
                        // Valid radius entered by the user
                        addGeofenceMarker(latLng, radius)
                        startGeofence()
                    } else {
                        Toast.makeText(requireContext(), "Invalid radius entered", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

            builder.show()
        }
    }

    private fun showPermissionDeniedMessageOrNavigateToSettings() {
        PermissionManager.showPermissionDeniedDialog(
            requireContext(),
            DialogInterface.OnClickListener { _, _ -> PermissionManager.openAppSettings(requireContext()) }
        )
    }
    private fun showCustomerInfoBottomSheet() {
        val bottomSheetFragment = MultipleGeofenceListBottomSheetFragment(geofenceList)
        bottomSheetFragment.isCancelable = false
        bottomSheetFragment.show(
            requireActivity().supportFragmentManager,
            bottomSheetFragment.tag
        )
    }

}
