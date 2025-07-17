package com.example.traingym.trainer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.traingym.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SuspendedMembersFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var suspendedMembersAdapter: SuspendedMembersAdapter
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_suspended_members, container, false)
        lottieAnimationView = view.findViewById(R.id.lottie_loading_animation_suspended)
        recyclerView = view.findViewById(R.id.suspended_members_recycler_view)

        setupRecyclerView()
        return view
    }

    override fun onResume() {
        super.onResume()
        val mainActivity = activity as? AppCompatActivity
        mainActivity?.findViewById<TextView>(R.id.toolbar_title)?.text = "Suspended Accounts"
        mainActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fetchSuspendedMembers()
    }

    override fun onStop() {
        super.onStop()
        val mainActivity = activity as? AppCompatActivity
        mainActivity?.findViewById<TextView>(R.id.toolbar_title)?.text = "Member List"
        mainActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun setupRecyclerView() {
        suspendedMembersAdapter = SuspendedMembersAdapter { member ->
            activateMember(member)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = suspendedMembersAdapter
        }
    }

    private fun fetchSuspendedMembers() {
        showLoading(true)
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("AppUsers")
                    .whereEqualTo("_suspended", true)
                    .get()
                    .await()
                val members = snapshot.toObjects(Member::class.java)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    suspendedMembersAdapter.updateMembers(members)
                    if (members.isEmpty()) {
                        Toast.makeText(context, "No suspended members found.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun activateMember(member: Member) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Re-activate User?")
            .setMessage("Are you sure you want to re-activate ${member.username}?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Re-activate") { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    try {
                        firestore.collection("AppUsers").document(member.uid)
                            .update("_suspended", false)
                            .await()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "User re-activated.", Toast.LENGTH_SHORT).show()
                            fetchSuspendedMembers()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Activation failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
            .show()
    }

    private fun showLoading(isLoading: Boolean) {
        lottieAnimationView.visibility = if (isLoading) View.VISIBLE else View.GONE
        recyclerView.visibility = if (isLoading) View.GONE else View.VISIBLE
    }
}