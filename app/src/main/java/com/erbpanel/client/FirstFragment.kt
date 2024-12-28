package com.erbpanel.client

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.erbpanel.client.adapters.VehicleInfo
import com.erbpanel.client.databinding.FragmentFirstBinding
import com.erbpanel.client.utils.checkAndAssignRole
import com.erbpanel.client.utils.getRole
import com.erbpanel.client.utils.showErrorDialog
import com.erbpanel.client.utils.showSuccessDialog
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson

class FirstFragment : Fragment() {

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    private val RC_SIGN_IN = 100
    private var _binding: FragmentFirstBinding? = null
    private lateinit var codeScanner: CodeScanner
    private val binding get() = _binding!!
    private lateinit var db:FirebaseFirestore
    private var currentUser: FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        firebaseAuth = FirebaseAuth.getInstance()

        // Check if the user is already signed in
        currentUser = firebaseAuth.currentUser
        if (currentUser == null) {

            // Configure Google Sign-In
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

            // Start the Google Sign-In intent
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        } else {
            // User is already signed in
            onUserLoggedIn(currentUser)
        }

        (activity as? AppCompatActivity)?.supportActionBar?.hide()
        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        codeScanner = CodeScanner(requireContext(), binding.scannerView)

        // Configure code scanner
        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = CodeScanner.ALL_FORMATS
        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.CONTINUOUS
        codeScanner.isAutoFocusEnabled = true
        codeScanner.isFlashEnabled = false

        // Set decode callback
        codeScanner.decodeCallback = DecodeCallback {
            codeScanner.stopPreview()
            Log.d("TAG", it.text)
            view.post {
                val vehicleInfo = Gson().fromJson(it.text, VehicleInfo::class.java)
                db = FirebaseFirestore.getInstance()
                vehicleInfo.userId = currentUser?.uid
                db.collection("trip").add(vehicleInfo)
                    .addOnFailureListener { err ->
                        run {
                            showErrorDialog(requireContext(), err.message.toString())
                        }
                    }.addOnSuccessListener {
                        run {
                        showSuccessDialog(
                            requireContext(),
                            "Trip added successfully",
                            codeScanner,
                            findNavController(),
                            cancelable = false)
                        } }
                Toast.makeText(requireContext(), "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
            }
        }

        // Set error callback
        codeScanner.errorCallback = ErrorCallback {
            view.post {
                Toast.makeText(requireContext(), "Camera initialization error: ${it.message}", Toast.LENGTH_LONG).show()
            }
        }

        binding.scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(Exception::class.java)
            if (account != null) {
                firebaseAuthWithGoogle(account)
            }
        } catch (e: Exception) {
            Log.d("TAG", e.message.toString())
            Toast.makeText(requireContext(), "Sign-in failed."+e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = firebaseAuth.currentUser
                    onUserLoggedIn(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(requireContext(), "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun onUserLoggedIn(user: FirebaseUser?) {
        Toast.makeText(requireContext(), "Welcome, ${user?.displayName}!", Toast.LENGTH_SHORT).show()

        if (user != null) {
            checkAndAssignRole(user.uid)
            getRole(userId = user.uid) { role ->
                if (role != null && role.equals("driver", ignoreCase = true)) {
                    findNavController().navigate(R.id.action_FirstFragment_to_QRGenerator)
                } else {
                    // Handle the case when role is null or error occurred
                    Log.d("ROLE", "Failed to fetch role.")
                }
            }

        }
    }
}