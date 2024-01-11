package com.nivi.multiplegeofence.ui


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nivi.multiplegeofence.R
import com.nivi.multiplegeofence.data.model.LatLngWithCustomer

class CustomerInfoAdapter(private val customerList: List<LatLngWithCustomer>) :
    RecyclerView.Adapter<CustomerInfoAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewCustomerName: TextView = itemView.findViewById(R.id.textViewCustomerName)
        val textViewCustomerAddress: TextView = itemView.findViewById(R.id.textViewCustomerAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_customer_info, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val customer = customerList[position]
        holder.textViewCustomerName.text = customer.customerName
        holder.textViewCustomerAddress.text = getAddressDetails(
            customer.latLng.latitude,
            customer.latLng.longitude
        )
    }

    override fun getItemCount(): Int {
        return customerList.size
    }

    private fun getAddressDetails(latitude: Double, longitude: Double): String {
        // Implement the logic to get the address details as needed
        // You can use the existing logic from the RouteMapFragment for this purpose
        // For simplicity, returning a dummy address
        return "Address for $latitude, $longitude"
    }
}
