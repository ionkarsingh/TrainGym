package com.example.traingym.user

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.traingym.R
import com.example.traingym.auth.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class UserProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var roleTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var loadingOverlay: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        usernameTextView = view.findViewById(R.id.text_view_username)
        emailTextView = view.findViewById(R.id.text_view_email)
        roleTextView = view.findViewById(R.id.text_view_role)
        phoneTextView = view.findViewById(R.id.text_view_phone)
        addressTextView = view.findViewById(R.id.text_view_address)
        val logoutButton = view.findViewById<Button>(R.id.button_logout)
        loadingOverlay = view.findViewById(R.id.loading_overlay)

        logoutButton.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchUserProfileData()
    }

    private fun showLoading(isLoading: Boolean) {
        loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun fetchUserProfileData() {
        showLoading(true)
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "User not logged in.", Toast.LENGTH_SHORT).show()
            showLoading(false)
            return
        }

        firestore.collection("AppUsers").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val username = document.getString("username")
                    val email = document.getString("email")
                    val userType = "Gym Member"
                    val phone = document.getString("phone")
                    val address = document.getString("address")

                    usernameTextView.text = username
                    emailTextView.text = email
                    roleTextView.text = userType
                    phoneTextView.text = phone
                    addressTextView.text = address

                } else {
                    Toast.makeText(context, "User data not found.", Toast.LENGTH_SHORT).show()
                }
                showLoading(false)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error fetching profile: ${e.message}", Toast.LENGTH_SHORT).show()
                showLoading(false)
            }
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Logout") { _, _ ->
                logoutUser()
            }
            .show()
    }

    private fun logoutUser() {
        showLoading(true)
        auth.signOut()
        val intent = Intent(activity, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        activity?.finish()
    }
} 