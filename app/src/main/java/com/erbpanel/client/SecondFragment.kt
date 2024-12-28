package com.erbpanel.client

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.erbpanel.client.adapters.TractorAdapter
import com.erbpanel.client.adapters.VehicleInfo
import com.erbpanel.client.databinding.FragmentSecondBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.type.DateTime

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var user: FirebaseUser
    private lateinit var adapter: TractorAdapter

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        user = auth.currentUser!!

        Log.d("TAG", user.uid)
        Toast.makeText(requireContext(), user.uid, Toast.LENGTH_SHORT).show()
        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val trips = db.collection("trip")
        val tripInfo = ArrayList<VehicleInfo>()
        var adapter = TractorAdapter(requireContext(), tripInfo)

        trips.whereEqualTo(
            "userId", user.uid,
        ).get()
            .addOnSuccessListener{ docs ->
                val tripList = docs.map { it.data }
                tripInfo.addAll(tripList.map { data ->
                    VehicleInfo(
                        data["driverName"] as String,
                        data["vehicleNo"] as String,
                        data["rate"] as Long,
                        data["dateTime"] as Timestamp
                    )
                })
                Log.d("TAG 2", tripInfo.toString())

                adapter = TractorAdapter(requireContext(), tripInfo)
                binding.scanList.adapter = adapter

            }.addOnFailureListener { error->
                Log.d("TAG", error.message.toString())
            }

            binding.scanList.adapter = adapter
            adapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}