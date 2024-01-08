// src/main/java/com/nivi/multiplegeofence/GeofenceItem.kt
package com.nivi.multiplegeofence.data.model

import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.Marker


class GeofenceItem(
    val marker: Marker,val circle: Circle, val onDeleteClickListener: () -> Unit,
)

