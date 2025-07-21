package com.example.traingym.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.traingym.R
import com.example.traingym.admin.AdminMainActivity
import com.example.traingym.trainer.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

class SplashActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        CoroutineScope(Dispatchers.Main).launch {
            delay(2000)
            checkUserStatus()
        }
    }

    private suspend fun checkUserStatus() {
        if (auth.currentUser == null) {
            navigateToLogin()
        } else {
            checkUserRoleAndNavigate()
        }
    }

    private suspend fun checkUserRoleAndNavigate() {
        val userId = auth.currentUser?.uid ?: run {
            navigateToLogin()
            return
        }

        try {
            val doc = withContext(Dispatchers.IO) {
                firestore.collection("AppUsers").document(userId).get().await()
            }

            if (doc.exists()) {
                val userType = doc.getString("user_type")
                when (userType) {
                    "trainer" -> navigateTo(MainActivity::class.java)
                    "admin" -> navigateTo(AdminMainActivity::class.java)
                    else -> {
                        auth.signOut()
                        navigateToLogin()
                    }
                }
            } else {
                auth.signOut()
                navigateToLogin()
            }
        } catch (e: Exception) {
            auth.signOut()
            navigateToLogin()
        }
    }

    private suspend fun navigateToLogin() {
        withContext(Dispatchers.Main) {
            startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            finish()
        }
    }

    private suspend fun <T> navigateTo(activityClass: Class<T>) {
        withContext(Dispatchers.Main) {
            startActivity(Intent(this@SplashActivity, activityClass))
            finish()
        }
    }
}