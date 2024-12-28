package com.erbpanel.client

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.erbpanel.client.adapters.VehicleInfo
import com.erbpanel.client.databinding.FragmentQRGeneratorBinding
import com.erbpanel.client.utils.generateQRCode
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import java.util.Date

class QRGenerator : Fragment() {
    private var _binding: FragmentQRGeneratorBinding? = null
    private lateinit var user: FirebaseUser



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        var auth = FirebaseAuth.getInstance()
        user = auth.currentUser!!
        _binding = FragmentQRGeneratorBinding.inflate(inflater, container, false)
        val vehicleInfo = VehicleInfo(
            driverName = "John Doe",
            vehicleNo = "AS01DS0934",
            rate = 450,
            dateTime = Timestamp(Date()),
            driverId = user.uid
        )
        val qrBitmap = generateQRCode(vehicleInfo)
        if (qrBitmap != null) {
            _binding?.qrImageView?.setImageBitmap(qrBitmap)
        } else {
            println("Failed to generate QR Code.")
        }

        _binding?.btnDownloadQr?.setOnClickListener {
            // Handle download button click
        }
        _binding?.btnMyTrip?.setOnClickListener{
            findNavController().navigate(R.id.action_FirstFragment_to_QRGenerator)
        }

        return _binding?.root
    }
}