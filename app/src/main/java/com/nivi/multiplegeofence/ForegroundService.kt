// ForegroundService.kt
package com.nivi.multiplegeofence

import android.Manifest
import android.app.*
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
import androidx.work.*
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.nivi.multiplegeofence.GeofenceWorker
import java.util.concurrent.TimeUnit
import com.nivi.multiplegeofence.R

class ForegroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notificationId = System.currentTimeMillis().toInt()
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Multiple Geofence Service")
            .setContentText("Running in the background")
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .build()

        startForeground(notificationId, notification)

        handleGeofenceEvents(intent)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun handleGeofenceEvents(intent: Intent?) {
        // Check if the intent is not null and contains geofencing events
        if (intent != null) {
            val geofencingEvent = GeofencingEvent.fromIntent(intent)
            if (geofencingEvent != null && !geofencingEvent.hasError()) {
                val geofenceTransition = geofencingEvent.geofenceTransition
                val geofenceList = geofencingEvent.triggeringGeofences

                if (!geofenceList.isNullOrEmpty()) {
                    val inputData = workDataOf(
                        WORKER_DATA_GEOFENCE_ID to geofenceList[0].requestId,
                        WORKER_DATA_TRANSITION_TYPE to geofenceTransition
                    )

                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()

                    val geofenceWorkRequest = OneTimeWorkRequestBuilder<GeofenceWorker>()
                        .setInputData(inputData)
                        .setConstraints(constraints)
                        .build()

                    WorkManager.getInstance(this)
                        .enqueueUniqueWork(
                            WORKER_TAG,
                            ExistingWorkPolicy.APPEND_OR_REPLACE,
                            geofenceWorkRequest
                        )
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Geofence Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "GeofenceServiceChannel"
        const val NOTIFICATION_ID = 1
        private const val WORKER_TAG = "GeofenceWorker"
        const val WORKER_DATA_GEOFENCE_ID = "GEOFENCE_ID"
        const val WORKER_DATA_TRANSITION_TYPE = "TRANSITION_TYPE"
    }
}
