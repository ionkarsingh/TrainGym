package com.example.traingym.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.traingym.R
import com.example.traingym.admin.AdminMainActivity
import com.example.traingym.trainer.MainActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var emailEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var loginButton: Button
    private lateinit var lottieAnimationView: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.edit_text_email)
        passwordEditText = findViewById(R.id.edit_text_password)
        loginButton = findViewById(R.id.button_login)
        lottieAnimationView = findViewById(R.id.lottie_loading_animation)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            showLoading(true)
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        checkUserRoleAndNavigate()
                    } else {
                        showLoading(false)
                        Toast.makeText(this, "Login Failed: Invalid email or password.", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            lottieAnimationView.visibility = View.VISIBLE
            loginButton.isEnabled = false
            emailEditText.isEnabled = false
            passwordEditText.isEnabled = false
        } else {
            lottieAnimationView.visibility = View.GONE
            loginButton.isEnabled = true
            emailEditText.isEnabled = true
            passwordEditText.isEnabled = true
        }
    }

    private fun checkUserRoleAndNavigate() {
        val userId = auth.currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val doc = firestore.collection("AppUsers").document(userId).get().await()
                if (doc.exists()) {
                    val userType = doc.getString("user_type")
                    val isSuspended = doc.getBoolean("_suspended") ?: false

                    withContext(Dispatchers.Main) {
                        if (isSuspended) {
                            showLoading(false)
                            MaterialAlertDialogBuilder(this@LoginActivity)
                                .setTitle("Login Failed")
                                .setMessage("You are not authorized to log in. Your account has been suspended by the admin.")
                                .setPositiveButton("OK", null)
                                .show()
                            auth.signOut()
                        } else {
                            when (userType) {
                                "trainer" -> {
                                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                                    finish()
                                }
                                "admin" -> {
                                    startActivity(Intent(this@LoginActivity, AdminMainActivity::class.java))
                                    finish()
                                }
                                else -> {
                                    showLoading(false)
                                    Toast.makeText(this@LoginActivity, "You are not authorized to log in.", Toast.LENGTH_LONG).show()
                                    auth.signOut()
                                }
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showLoading(false)
                        auth.signOut()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    Toast.makeText(this@LoginActivity, "Error checking user status: ${e.message}", Toast.LENGTH_LONG).show()
                    auth.signOut()
                }
            }
        }
    }
}