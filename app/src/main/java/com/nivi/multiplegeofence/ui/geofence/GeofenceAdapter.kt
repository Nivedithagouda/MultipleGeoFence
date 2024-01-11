// src/main/java/com/nivi/multiplegeofence/GeofenceAdapter.kt
package com.nivi.multiplegeofence.ui.geofence

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nivi.multiplegeofence.R
import com.nivi.multiplegeofence.data.model.GeofenceItem

class GeofenceAdapter(private val geofenceList: MutableList<GeofenceItem>) :
    RecyclerView.Adapter<GeofenceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.geofence_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val geofenceItem = geofenceList[position]
        holder.geofenceTitle.text = geofenceItem.marker.title
        holder.geofenceDetails.text =
            "Lat: ${geofenceItem.marker.position.latitude},\nLng: ${geofenceItem.marker.position.longitude}"
        holder.btnDelete.setOnClickListener {
            // Call a method to handle the delete action
            geofenceItem.onDeleteClickListener.invoke()
        }
    }

    override fun getItemCount(): Int {
        return geofenceList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val geofenceTitle: TextView = itemView.findViewById(R.id.geofenceTitle)
        val geofenceDetails: TextView = itemView.findViewById(R.id.geofenceDetails)
        val btnDelete:Button=itemView.findViewById(R.id.btnDelete)
    }
}
