package com.erbpanel.client.utils

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlin.math.log

fun getRole(userId: String, callback: (String?) -> Unit){
    val db = FirebaseFirestore.getInstance()
    val userRef:DocumentReference = db.collection("users").document(userId)
    userRef.get()
        .addOnSuccessListener { document ->
            // Check if the document exists and fetch the role
            val role = document.getString("role") // This will return the role or null if not present
            Log.d("ROLE", "getRole: $role")
            callback(role) // Invoke callback with the role value
        }
        .addOnFailureListener { err ->
            // If there's an error, return null
            Log.d("ROLE", "getRole failed: ${err.message}")
            callback(null) // Invoke callback with null on failure
        }
}
fun checkAndAssignRole(userId: String): String? {
    val db = FirebaseFirestore.getInstance()
    val userRef = db.collection("users").document(userId)
    var role: String? = null
    // Check if role is already assigned
    userRef.get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val isRoleAssigned = document.getBoolean("isRoleAssigned") ?: false
                if (!isRoleAssigned) {
                    assignDefaultRole(userRef)
                } else {
                    Log.d("TAG", "Role already assigned")
                    role = document.getString("role")
                }
            } else {
                assignDefaultRole(userRef)
            }
        }
        .addOnFailureListener { exception ->
            Log.w("TAG", "Error checking role", exception)
        }
    return null
}

    // Assign a default role and mark it as assigned
private fun assignDefaultRole(userRef: DocumentReference) {
    val defaultRoleData = mapOf(
        "role" to "user",
        "isRoleAssigned" to true
    )

    userRef.set(defaultRoleData, SetOptions.merge())
        .addOnSuccessListener {
            Log.d("TAG", "Default role assigned successfully")
        }
        .addOnFailureListener { exception ->
            Log.w("TAG", "Error assigning default role", exception)
        }
}
