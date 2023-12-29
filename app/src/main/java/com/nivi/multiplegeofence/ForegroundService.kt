package com.nivi.multiplegeofence

// ForegroundService.kt

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

class ForegroundService : Service() {



    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Multiple Geofence Service")
            .setContentText("Running in the background")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        handleGeofenceEvents(intent)
        // Your geofencing code can go here or be triggered from the notification

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun handleGeofenceEvents(intent: Intent?) {
        // Check if the intent is not null and contains geofencing events
        if (intent != null) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent != null) {
                if (geofencingEvent.hasError()) {
                    Log.e(TAG, "GeofencingEvent error: ${geofencingEvent.errorCode}")
                    return
                }
            }

            // Handle geofencing events based on the transition type
            val geofenceTransition = geofencingEvent?.geofenceTransition
            val geofenceList = geofencingEvent?.triggeringGeofences

            if (geofenceList != null) {
                for (geofence in geofenceList) {
                    val geofenceId = geofence.requestId
                    when (geofenceTransition) {
                        Geofence.GEOFENCE_TRANSITION_ENTER -> {
                            Log.d(TAG, "Geofence enter transition for geofence $geofenceId")
                            showNotification(this,"Entered Geofence $geofenceId")
                        }

                        Geofence.GEOFENCE_TRANSITION_EXIT -> {
                            Log.d(TAG, "Geofence exit transition for geofence $geofenceId")
                            showNotification(this,"Exited Geofence $geofenceId")
                        }

                        Geofence.GEOFENCE_TRANSITION_DWELL -> {
                            Log.d(TAG, "Geofence dwell transition for geofence $geofenceId")
                            showNotification(this,"Dwelling in Geofence $geofenceId")
                        }

                        else -> {
                            // Handle unknown geofence transition type
                            Log.e(TAG, "Unknown geofence transition type: $geofenceTransition")
                        }
                    }
                }
            }
        }
    }
    private fun showNotification(context: Context?, message: String) {
        if (ContextCompat.checkSelfPermission(
                context!!,
                Manifest.permission.VIBRATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted, proceed with creating the notification channel
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                   CHANNEL_ID,
                    "Geofence Channel",
                    NotificationManager.IMPORTANCE_HIGH
                )
                val notificationManager = context.getSystemService(NotificationManager::class.java)
                notificationManager?.createNotificationChannel(channel)
            }

            // Generate a unique notification ID using the current timestamp
            val notificationId = System.currentTimeMillis().toInt()

            // Create the notification
            val notification = NotificationCompat.Builder(context,
               CHANNEL_ID
            )
                .setContentTitle("Geofence Notification")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_notification)
                .setDefaults(Notification.DEFAULT_ALL) // Include all default behaviors
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

            // Notify with the unique notification ID
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.notify(notificationId, notification)
        } else {
            // Permission is not granted, request it from the user
            Toast.makeText(
                context,
                "no permission",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Geofence Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "GeofenceServiceChannel"
        const val NOTIFICATION_ID = 1
        private const val TAG = "GeofenceService"
    }
}
