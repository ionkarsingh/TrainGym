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
import com.example.traingym.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AdminTrainersFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_admin_trainers, container, false)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab_add_trainer)
        fab.setOnClickListener {
            showAddTrainerDialog()
        }
        return view
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
                            "user_type" to "trainer"
                        )
                        firestore.collection("AppUsers").document(uid).set(newTrainer).await()

                        Toast.makeText(context, "Trainer added successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error adding trainer: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
        dialog.show()
    }
}