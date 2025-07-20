package com.example.traingym.admin

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.traingym.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminTrainersFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var trainersRecyclerView: RecyclerView
    private lateinit var trainerAdapter: TrainerAdapter
    private lateinit var lottieAnimationView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_trainers, container, false)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab_add_trainer)
        trainersRecyclerView = view.findViewById(R.id.trainers_recycler_view)
        lottieAnimationView = view.findViewById(R.id.lottie_loading_animation)

        setupRecyclerView()

        fab.setOnClickListener {
            showAddTrainerDialog()
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        fetchTrainers()
    }

    private fun setupRecyclerView() {
        trainerAdapter = TrainerAdapter(emptyList(),
            onEditClick = { trainer ->
                showEditTrainerDialog(trainer)
            },
            onSuspendClick = { trainer ->
                suspendTrainer(trainer)
            }
        )
        trainersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = trainerAdapter
        }
    }

    private fun showLoading(isLoading: Boolean) {
        lottieAnimationView.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun fetchTrainers() {
        showLoading(true)
        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("AppUsers")
                    .whereEqualTo("user_type", "trainer")
                    .whereEqualTo("_suspended", false)
                    .get().await()
                val trainers = snapshot.toObjects(Trainer::class.java)
                trainerAdapter.updateData(trainers)
            } catch (e: Exception) {
                Toast.makeText(context, "Error fetching trainers: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                showLoading(false)
            }
        }
    }

    private fun showAddTrainerDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_trainer, null)
        val usernameEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_username)
        val emailEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_email)
        val passwordEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_password)
        val saveButton = dialogView.findViewById<Button>(R.id.button_save_trainer)
        val closeButton = dialogView.findViewById<ImageView>(R.id.image_view_close_dialog)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        closeButton.setOnClickListener { dialog.dismiss() }

        saveButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (username.isEmpty()) {
                usernameEditText.error = "Username is required"
                return@setOnClickListener
            }
            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailEditText.error = "A valid email is required"
                return@setOnClickListener
            }
            if (password.length < 6) {
                passwordEditText.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }
            lifecycleScope.launch {
                try {
                    val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                    val uid = authResult.user?.uid

                    if (uid != null) {
                        val newTrainer = hashMapOf(
                            "uid" to uid,
                            "username" to username,
                            "email" to email,
                            "user_type" to "trainer",
                            "addedDate" to FieldValue.serverTimestamp(),
                            "_suspended" to false
                        )
                        firestore.collection("AppUsers").document(uid).set(newTrainer).await()

                        Toast.makeText(context, "Trainer added successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        fetchTrainers()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error adding trainer: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        dialog.show()
    }

    private fun showEditTrainerDialog(trainer: Trainer) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_trainer, null)
        val usernameEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_trainer_username)
        usernameEditText.setText(trainer.username)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Trainer")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val newUsername = usernameEditText.text.toString().trim()
                if (newUsername.isNotEmpty()) {
                    firestore.collection("AppUsers").document(trainer.uid)
                        .update("username", newUsername)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Username updated!", Toast.LENGTH_SHORT).show()
                            fetchTrainers()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
            }
            .show()
    }

    private fun suspendTrainer(trainer: Trainer) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Suspend Trainer")
            .setMessage("Are you sure you want to suspend ${trainer.username}?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Suspend") { _, _ ->
                firestore.collection("AppUsers").document(trainer.uid)
                    .update("_suspended", true)
                    .addOnSuccessListener {
                        Toast.makeText(context, "${trainer.username} has been suspended.", Toast.LENGTH_SHORT).show()
                        fetchTrainers()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .show()
    }
}