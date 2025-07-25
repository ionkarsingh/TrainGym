package com.example.traingym.admin

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
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
    private lateinit var noTrainersTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_trainers, container, false)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab_add_trainer)
        trainersRecyclerView = view.findViewById(R.id.trainers_recycler_view)
        lottieAnimationView = view.findViewById(R.id.lottie_loading_animation)
        noTrainersTextView = view.findViewById(R.id.text_view_no_trainers)

        setupRecyclerView()

        fab.setOnClickListener {
            showAddTrainerDialog()
        }
        return view
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.admin_trainers_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_view_suspended_trainers -> {
                parentFragmentManager.beginTransaction()
                    .replace(R.id.admin_fragment_container, SuspendedTrainersFragment())
                    .addToBackStack(null)
                    .commit()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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
        trainersRecyclerView.visibility = View.GONE
        noTrainersTextView.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val snapshot = firestore.collection("AppUsers")
                    .whereEqualTo("user_type", "trainer")
                    .whereEqualTo("_suspended", false)
                    .get().await()
                val trainers = snapshot.toObjects(Trainer::class.java)

                if (trainers.isEmpty()) {
                    noTrainersTextView.visibility = View.VISIBLE
                    trainersRecyclerView.visibility = View.GONE
                } else {
                    noTrainersTextView.visibility = View.GONE
                    trainersRecyclerView.visibility = View.VISIBLE
                    trainerAdapter.updateData(trainers)
                }
            } catch (e: Exception) {
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
        val phoneEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_phone)
        val addressEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_address)
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
            val phone = phoneEditText.text.toString().trim()
            val address = addressEditText.text.toString().trim()

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
            if (phone.length < 10) {
                phoneEditText.error = "Phone number must be at least 10 digits"
                return@setOnClickListener
            }
            if (address.isEmpty()) {
                addressEditText.error = "Address is required"
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
                            "_suspended" to false,
                            "phone" to phone,
                            "address" to address
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
        val usernameEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_edit_username)
        val phoneEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_edit_phone)
        val addressEditText = dialogView.findViewById<TextInputEditText>(R.id.edit_text_edit_address)

        usernameEditText.setText(trainer.username)
        phoneEditText.setText(trainer.phone)
        addressEditText.setText(trainer.address)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Trainer")
            .setView(dialogView)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Save") { _, _ ->
                val newUsername = usernameEditText.text.toString().trim()
                val newPhone = phoneEditText.text.toString().trim()
                val newAddress = addressEditText.text.toString().trim()

                if (newUsername.isNotEmpty()) {
                    val updates = mapOf(
                        "username" to newUsername,
                        "phone" to newPhone,
                        "address" to newAddress
                    )
                    firestore.collection("AppUsers").document(trainer.uid)
                        .update(updates)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Details updated!", Toast.LENGTH_SHORT).show()
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