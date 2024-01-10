package com.nivi.multiplegeofence.ui.utility

    import android.Manifest
    import android.app.Activity
    import android.content.Context
    import android.content.DialogInterface
    import android.content.Intent
    import android.content.pm.PackageManager
    import android.net.Uri
    import androidx.appcompat.app.AlertDialog
    import androidx.core.app.ActivityCompat
    import androidx.core.content.ContextCompat
    import android.provider.Settings

    object PermissionManager {

        fun checkLocationPermission(context: Context, activity: Activity, requestCode: Int): Boolean {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )

            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestLocationPermission(context,activity, permissions, requestCode)
                return false
            } else {
                return true
            }
        }

        private fun requestLocationPermission(
            context: Context,
            activity: Activity,
            permissions: Array<String>,
            requestCode: Int
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) || ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            ) {
                // Show an explanation to the user and then request the permission again
                showPermissionExplanationDialog(context,activity, permissions, requestCode)
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(
                    activity,
                    permissions,
                    requestCode
                )
            }
        }

        private fun showPermissionExplanationDialog(
            context: Context,
            activity: Activity,
            permissions: Array<String>,
            requestCode: Int
        ) {
            AlertDialog.Builder(activity)
                .setTitle("Location Permission Required")
                .setMessage("This app requires location permission to function properly.")
                .setPositiveButton("OK") { _, _ ->
                 openAppSettings(context)
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        fun checkPermissionGranted(
            grantResults: IntArray,
            requestCode: Int,
            expectedCode: Int
        ): Boolean {
            return requestCode == expectedCode &&
                    grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED
        }

        fun showPermissionDeniedDialog(context: Context, onClickListener: DialogInterface.OnClickListener) {
            AlertDialog.Builder(context)
                .setTitle("Permission Denied")
                .setMessage("You need to grant location permission for this app to work.")
                .setPositiveButton("Settings", onClickListener)
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }


        fun openAppSettings(context: Context) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", context.packageName, null)
            intent.data = uri
            context.startActivity(intent)
        }

    }

