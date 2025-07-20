package com.example.traingym.admin

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
import com.example.traingym.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SuspendedTrainersFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var suspendedAdapter: SuspendedTrainerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_suspended_trainers, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.suspended_trainers_recycler_view)
        suspendedAdapter = SuspendedTrainerAdapter(emptyList()) { trainer ->
            reactivateTrainer(trainer)
        }
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = suspendedAdapter
        }

        fetchSuspendedTrainers()
        return view
    }

    override fun onResume() {
        super.onResume()
        val mainActivity = activity as? AppCompatActivity
        mainActivity?.findViewById<TextView>(R.id.toolbar_title)?.text = "Suspended Trainers"
        mainActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onStop() {
        super.onStop()
        val mainActivity = activity as? AppCompatActivity
        mainActivity?.findViewById<TextView>(R.id.toolbar_title)?.text = "Trainers"
        mainActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun fetchSuspendedTrainers() {
        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("AppUsers")
                    .whereEqualTo("user_type", "trainer")
                    .whereEqualTo("_suspended", true)
                    .get().await()
                val trainers = snapshot.toObjects(Trainer::class.java)
                suspendedAdapter.updateData(trainers)
                if (trainers.isEmpty()) {
                    Toast.makeText(context, "No suspended trainers found.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun reactivateTrainer(trainer: Trainer) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Re-activate Trainer")
            .setMessage("Are you sure you want to re-activate ${trainer.username}?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Re-activate") { _, _ ->
                firestore.collection("AppUsers").document(trainer.uid)
                    .update("_suspended", false)
                    .addOnSuccessListener {
                        Toast.makeText(context, "${trainer.username} has been re-activated.", Toast.LENGTH_SHORT).show()
                        fetchSuspendedTrainers()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .show()
    }
}