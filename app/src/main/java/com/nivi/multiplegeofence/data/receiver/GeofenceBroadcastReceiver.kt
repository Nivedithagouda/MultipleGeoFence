// src/main/java/com/nivi/multiplegeofence/GeofenceBroadcastReceiver.kt
package com.nivi.multiplegeofence.data.receiver

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.nivi.multiplegeofence.R

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "GeofenceBroadcastReceiver onReceive")

        val geofencingEvent = intent?.let { GeofencingEvent.fromIntent(it) }
        if (geofencingEvent != null) {
            if (geofencingEvent.hasError()) {
                Log.e(TAG, "GeofencingEvent error: ${geofencingEvent.errorCode}")
                return
            }

            val geofenceTransition = geofencingEvent.geofenceTransition
            val geofenceList = geofencingEvent.triggeringGeofences

            if (geofenceList != null) {
                for (geofence in geofenceList) {
                    val geofenceId = geofence.requestId
                    when (geofenceTransition) {
                        Geofence.GEOFENCE_TRANSITION_ENTER -> {
                            Log.d(TAG, "Geofence enter transition for geofence $geofenceId")
                            showNotification(context, "Entered Geofence $geofenceId")
                        }

                        Geofence.GEOFENCE_TRANSITION_EXIT -> {
                            Log.d(TAG, "Geofence exit transition for geofence $geofenceId")
                            showNotification(context, "Exited Geofence $geofenceId")
                        }

                        Geofence.GEOFENCE_TRANSITION_DWELL -> {
                            Log.d(TAG, "Geofence dwell transition for geofence $geofenceId")
                            showNotification(context, "Dwelling in Geofence $geofenceId")
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
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
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





    companion object {
        private const val TAG = "GeofenceReceiver"
        private const val CHANNEL_ID = "GeofenceChannel"
        private const val NOTIFICATION_ID = 123
    }
}