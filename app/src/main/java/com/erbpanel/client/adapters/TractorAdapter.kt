package com.erbpanel.client.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.erbpanel.client.R
import com.google.firebase.Timestamp
import com.google.type.DateTime

data class VehicleInfo(
    val driverName: String,
    val vehicleNo: String,
    val rate: Long,
    val dateTime: Timestamp,
    var userId: String? = null,
    var driverId: String? = null

)

class TractorAdapter(
    private val context: Context,
    private val vehicles: List<VehicleInfo>
) : BaseAdapter() {

    override fun getCount(): Int = vehicles.size

    override fun getItem(position: Int): Any = vehicles[position]

    override fun getItemId(position: Int): Long = position.toLong()

    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.card_item, parent, false)
        val vehicle = vehicles[position]

        val tractorIcon = view.findViewById<ImageView>(R.id.tractorIcon)
        val tvDriver = view.findViewById<TextView>(R.id.tvDriver)
        val tvVehicle = view.findViewById<TextView>(R.id.tvVehicle)
        val tvRate = view.findViewById<TextView>(R.id.tvRate)
        val tvDateTime = view.findViewById<TextView>(R.id.tvDateTime)

        // Populate the views
        tractorIcon.setImageResource(R.drawable.ic_bus_24) // Replace with your drawable
        tvDriver.text = "Driver: ${vehicle.driverName}"
        tvVehicle.text = "Vehicle No: ${vehicle.vehicleNo}"
        tvRate.text = "Rate: ₹ ${vehicle.rate}"
        tvDateTime.text = "Date Time: ${vehicle.dateTime.toDate()}"

        return view
    }
}
