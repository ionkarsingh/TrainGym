package com.example.traingym.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.traingym.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminHomeFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var adminNameTextView: TextView
    private lateinit var totalMembersCountTextView: TextView
    private lateinit var currentDateTextView: TextView
    private lateinit var activeTrainersCountTextView: TextView
    private lateinit var activeMembersCountTextView: TextView
    private lateinit var loadingOverlay: ConstraintLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = Firebase.firestore
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_home, container, false)
        adminNameTextView = view.findViewById(R.id.text_view_admin_name)
        totalMembersCountTextView = view.findViewById(R.id.text_view_total_members_count)
        currentDateTextView = view.findViewById(R.id.text_view_current_date)
        activeTrainersCountTextView = view.findViewById(R.id.text_view_active_trainers_count)
        activeMembersCountTextView = view.findViewById(R.id.text_view_active_members_count)
        loadingOverlay = view.findViewById(R.id.loading_overlay)

        return view
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun loadDashboardData() {
        showLoading(true)
        val sdf = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
        currentDateTextView.text = sdf.format(Date())

        val currentUserId = auth.currentUser?.uid
        if (currentUserId == null) {
            showLoading(false)
            return
        }

        lifecycleScope.launch {
            try {
                val adminNameDeferred = async { firestore.collection("AppUsers").document(currentUserId).get().await() }

                val totalMembersDeferred = async { firestore.collection("AppUsers")
                    .whereIn("user_type", listOf("trainer", "normal_user"))
                    .get().await() }

                val activeTrainersDeferred = async { firestore.collection("AppUsers")
                    .whereEqualTo("user_type", "trainer")
                    .whereEqualTo("_suspended", false)
                    .get().await() }

                val activeMembersDeferred = async { firestore.collection("AppUsers")
                    .whereEqualTo("user_type", "normal_user")
                    .whereEqualTo("_suspended", false)
                    .get().await() }
                val adminDoc = adminNameDeferred.await()
                val totalMembersSnapshot = totalMembersDeferred.await()
                val activeTrainersSnapshot = activeTrainersDeferred.await()
                val activeMembersSnapshot = activeMembersDeferred.await()
                adminNameTextView.text = adminDoc.getString("username") ?: "Admin"
                totalMembersCountTextView.text = totalMembersSnapshot.size().toString()
                activeTrainersCountTextView.text = activeTrainersSnapshot.size().toString()
                activeMembersCountTextView.text = activeMembersSnapshot.size().toString()

            } catch (e: Exception) {
                Toast.makeText(context, "Error loading dashboard: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}