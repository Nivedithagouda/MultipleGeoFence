package com.nivi.multiplegeofence

// GeofenceWorker.kt

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.android.gms.location.Geofence

class GeofenceWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val geofenceId = inputData.getString(ForegroundService.WORKER_DATA_GEOFENCE_ID)
        val transitionType = inputData.getInt(ForegroundService.WORKER_DATA_TRANSITION_TYPE, -1)

        if (geofenceId != null && transitionType != -1) {
            when (transitionType) {
                Geofence.GEOFENCE_TRANSITION_ENTER -> {
                    Log.d(TAG, "Geofence enter transition for geofence $geofenceId")
                    showNotification(applicationContext, "Entered Geofence $geofenceId")
                }

                Geofence.GEOFENCE_TRANSITION_EXIT -> {
                    Log.d(TAG, "Geofence exit transition for geofence $geofenceId")
                    showNotification(applicationContext, "Exited Geofence $geofenceId")
                }

                Geofence.GEOFENCE_TRANSITION_DWELL -> {
                    Log.d(TAG, "Geofence dwell transition for geofence $geofenceId")
                    showNotification(applicationContext, "Dwelling in Geofence $geofenceId")
                }

                else -> {
                    // Handle unknown geofence transition type
                    Log.e(TAG, "Unknown geofence transition type: $transitionType")
                }
            }
        }

        return Result.success()
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
                .setOngoing(true)
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
        private const val TAG = "GeofenceWorker"
        private const val CHANNEL_ID = "GeofenceChannel"
    }
}
