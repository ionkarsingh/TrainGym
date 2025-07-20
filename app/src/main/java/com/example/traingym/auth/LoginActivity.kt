package com.example.traingym.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.traingym.R
import com.example.traingym.admin.AdminMainActivity
import com.example.traingym.trainer.MainActivity
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val emailEditText = findViewById<TextInputEditText>(R.id.edit_text_email)
        val passwordEditText = findViewById<TextInputEditText>(R.id.edit_text_password)
        val loginButton = findViewById<Button>(R.id.button_login)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        checkUserRoleAndNavigate()
                    } else {
                        Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            checkUserRoleAndNavigate()
        }
    }
    private fun checkUserRoleAndNavigate() {
        val userId = auth.currentUser?.uid ?: return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val doc = firestore.collection("AppUsers").document(userId).get().await()
                if (doc.exists()) {
                    val userType = doc.getString("user_type")
                    withContext(Dispatchers.Main) {
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
                                Toast.makeText(this@LoginActivity, "You are not authorized to log in.", Toast.LENGTH_LONG).show()
                                auth.signOut()
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@LoginActivity, "User data not found.", Toast.LENGTH_LONG).show()
                        auth.signOut()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Error checking user status: ${e.message}", Toast.LENGTH_LONG).show()
                    auth.signOut()
                }
            }
        }
    }
}