package com.nivi.multiplegeofence.ui.route

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
import com.nivi.multiplegeofence.data.model.LatLngWithCustomer
import com.nivi.multiplegeofence.ui.route.CustomerInfoAdapter

class CustomerInfoBottomSheetFragment(private val customerList: List<LatLngWithCustomer>) :
    BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.bottom_sheet_customer_info, container, false)

        val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerViewCustomerInfo)
        recyclerView.layoutManager = LinearLayoutManager(context)
        val bt_close: ImageView = rootView.findViewById(R.id.bt_close)
        recyclerView.adapter = CustomerInfoAdapter(requireContext(),customerList)
        bt_close.setOnClickListener {
            // Dismiss the bottom sheet when the close button is clicked
            dismiss()
        }
        return rootView
    }
}
