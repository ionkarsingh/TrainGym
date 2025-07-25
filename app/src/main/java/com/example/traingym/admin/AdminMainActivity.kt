package com.example.traingym.admin

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.traingym.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminMainActivity : AppCompatActivity() {

    private lateinit var toolbarTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toolbarTitle = findViewById(R.id.toolbar_title)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.admin_bottom_navigation)

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_admin_home -> {
                    replaceFragment(AdminHomeFragment(), "Dashboard")
                    true
                }
                R.id.navigation_admin_trainers -> {
                    replaceFragment(AdminTrainersFragment(), "Trainers")
                    true
                }
                R.id.navigation_admin_profile -> {
                    replaceFragment(AdminProfileFragment(), "Profile")
                    true
                }
                else -> false
            }
        }

        if (savedInstanceState == null) {
            bottomNavigationView.selectedItemId = R.id.navigation_admin_home
        }
    }

    private fun replaceFragment(fragment: Fragment, title: String) {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.admin_fragment_container)
        if (currentFragment?.javaClass == fragment.javaClass) {
            return
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.admin_fragment_container, fragment)
            .commit()
        toolbarTitle.text = title
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}