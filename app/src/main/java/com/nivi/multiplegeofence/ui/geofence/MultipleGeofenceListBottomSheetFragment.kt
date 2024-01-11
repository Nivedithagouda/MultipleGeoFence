package com.nivi.multiplegeofence.ui.geofence

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.nivi.multiplegeofence.R
import com.nivi.multiplegeofence.data.model.GeofenceItem
import com.nivi.multiplegeofence.data.model.LatLngWithCustomer
import com.nivi.multiplegeofence.ui.route.CustomerInfoAdapter

class MultipleGeofenceListBottomSheetFragment(private val geofenceList: List<GeofenceItem>) :
    BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.bottom_sheet_customer_info, container, false)

        val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerViewCustomerInfo)
        val bt_close: ImageView = rootView.findViewById(R.id.bt_close)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = GeofenceAdapter(geofenceList as MutableList<GeofenceItem>)
        bt_close.setOnClickListener {
            // Dismiss the bottom sheet when the close button is clicked
            dismiss()
        }

        return rootView
    }
}
