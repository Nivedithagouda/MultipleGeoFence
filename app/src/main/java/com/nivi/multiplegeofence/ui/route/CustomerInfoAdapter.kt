package com.nivi.multiplegeofence.ui.route


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nivi.multiplegeofence.R
import com.nivi.multiplegeofence.data.model.LatLngWithCustomer
import com.nivi.multiplegeofence.ui.utility.getAddressDetails

class CustomerInfoAdapter(val context: Context, private val customerList: List<LatLngWithCustomer>) :
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
        holder.textViewCustomerAddress.text = getAddressDetails(context,
            customer.latLng.latitude,
            customer.latLng.longitude
        )
    }

    override fun getItemCount(): Int {
        return customerList.size
    }
}
