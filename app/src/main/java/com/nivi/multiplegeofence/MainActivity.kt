package com.nivi.multiplegeofence

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private lateinit var buttonStartGeofence: Button
    private lateinit var recyclerView: RecyclerView

    private val GEOFENCE_RADIUS = 100.0f
    private val GEOFENCE_REQ_CODE = 0
    private val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )
//    private val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    private val geofenceList = mutableListOf<GeofenceItem>()
    private val geofenceAdapter = GeofenceAdapter(geofenceList)
    private lateinit var geofenceDataStore: GeofenceDataStore

    val geofenceCheckHandler = Handler(Looper.getMainLooper())
    private val geofenceCheckRunnable = object : Runnable {
        override fun run() {
            startGeofence()
            geofenceCheckHandler.postDelayed(this, 60000) // Schedule next check after 1 minute
//            geofenceCheckHandler.postDelayed(this, 300000)// Schedule next check after 5 minute
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        geofenceDataStore = GeofenceDataStore(this)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        buttonStartGeofence = findViewById(R.id.buttonStartGeofence)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = geofenceAdapter

        buttonStartGeofence.setOnClickListener {
            startGeofence()
        }
        geofenceCheckHandler.post(geofenceCheckRunnable)

    }
    private fun loadGeofenceFromDataStore() {
        lifecycleScope.launch {
            geofenceDataStore.getGeofence().collect { geofenceList ->
                geofenceList?.let {
                    for (geofence in it) {
                        val latLng = LatLng(geofence.first, geofence.second)
                        addGeofenceMarker(latLng)
                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setOnMapClickListener { latLng ->
            addGeofenceMarker(latLng)
        }
        checkLocationPermission()
        loadGeofenceFromDataStore()
        startGeofence()
        startForegroundService(Intent(this, ForegroundService::class.java))
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                permissions,
                GEOFENCE_REQ_CODE
            )
        } else {
            map.isMyLocationEnabled = true
        }
    }
    private fun addGeofenceMarker(latLng: LatLng) {
        // Check if the geofence with the same LatLng already exists
        if (geofenceList.any { it.marker.position == latLng }) {
            // Geofence with the same LatLng already exists, do not add again
            return
        }

        val markerOptions = MarkerOptions()
            .position(latLng)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
            .title("Geofence Marker ${geofenceList.size}")
            .draggable(true)
        val circleOptions = CircleOptions()
            .center(latLng)
            .radius(GEOFENCE_RADIUS.toDouble())
            .strokeColor(ContextCompat.getColor(this, R.color.black))
            .fillColor(Color.GRAY)

        val circle = map.addCircle(circleOptions)
        val marker = map.addMarker(markerOptions)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        var geofenceItem: GeofenceItem? = null

        marker?.let {
            geofenceItem = GeofenceItem(it, circle) {
                // Handle delete action
                geofenceItem?.let { it1 -> deleteGeofence(it1, latLng) }
            }
        }
        lifecycleScope.launch {
            geofenceDataStore.saveGeofence(latLng.latitude, latLng.longitude)
        }

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

        val geofencingClient = LocationServices.getGeofencingClient(this)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            geofencingClient.addGeofences(request, pendingIntent)?.run {
                addOnSuccessListener {
//                    Toast.makeText(this@MainActivity, "Geofence added", Toast.LENGTH_SHORT).show()
                }
                addOnFailureListener { exception ->
                    Toast.makeText(
                        this@MainActivity,
                        "Geofence failed to add: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(this, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            this,
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
        if (requestCode == GEOFENCE_REQ_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                map.isMyLocationEnabled = true
            } else {
                Toast.makeText(
                    this,
                    "Location permission denied. Geofencing will not work.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    private fun deleteGeofence(geofenceItem: GeofenceItem, latLng: LatLng) {
        // Remove geofence from the map
        geofenceItem.marker.remove()
        geofenceItem.circle.remove()

        // Remove geofence from the list
        geofenceList.remove(geofenceItem)
        lifecycleScope.launch {
            geofenceDataStore.clearGeofence(latLng.latitude,latLng.longitude)
        }


        // Notify the adapter that the data set has changed
        geofenceAdapter.notifyDataSetChanged()
    }

}
