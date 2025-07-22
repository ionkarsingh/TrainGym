package com.example.traingym.user

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.example.traingym.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.activity.addCallback

class UserMainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var toolbarTitle: TextView

    private var activeFragment: Fragment? = null

    private val homeFragment = UserHomeFragment()
    private val exploreFragment = UserExploreExercisesFragment()
    private val bmiFragment = UserBmiFragment()
    private val profileFragment = UserProfileFragment()

    private val fragmentMap = mapOf(
        R.id.navigation_home to homeFragment,
        R.id.navigation_explore to exploreFragment,
        R.id.navigation_bmi to bmiFragment,
        R.id.navigation_profile to profileFragment
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_main)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbarTitle = findViewById(R.id.toolbar_title)
        bottomNavigationView = findViewById(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragment_container, homeFragment, "home_tag").hide(homeFragment)
                add(R.id.fragment_container, exploreFragment, "explore_tag").hide(exploreFragment)
                add(R.id.fragment_container, bmiFragment, "bmi_tag").hide(bmiFragment)
                add(R.id.fragment_container, profileFragment, "profile_tag").hide(profileFragment)
            }.commit()

            switchFragment(homeFragment, "Home")
            bottomNavigationView.selectedItemId = R.id.navigation_home
        } else {
            activeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            val fragments = supportFragmentManager.fragments
            fragments.forEach { fragment ->
                when (fragment.tag) {
                    "home_tag" -> if (fragment is UserHomeFragment) homeFragment
                    "explore_tag" -> if (fragment is UserExploreExercisesFragment) exploreFragment
                    "bmi_tag" -> if (fragment is UserBmiFragment) bmiFragment
                    "profile_tag" -> if (fragment is UserProfileFragment) profileFragment
                }
            }
            activeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        }

        onBackPressedDispatcher.addCallback(this) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
                val currentTopFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
                when (currentTopFragment) {
                    is UserHomeFragment -> toolbarTitle.text = "Home"
                    is UserExploreExercisesFragment -> toolbarTitle.text = "Explore Categories"
                    is UserBmiFragment -> toolbarTitle.text = "BMI Calculator"
                    is UserProfileFragment -> toolbarTitle.text = "Profile"
                }

            } else {
                isEnabled = false
                onBackPressedDispatcher.onBackPressed()
            }
        }

        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    switchFragment(homeFragment, "Home")
                    true
                }
                R.id.navigation_explore -> {
                    switchFragment(exploreFragment, "Explore Categories")
                    true
                }
                R.id.navigation_bmi -> {
                    switchFragment(bmiFragment, "BMI Calculator")
                    true
                }
                R.id.navigation_profile -> {
                    switchFragment(profileFragment, "Profile")
                    true
                }
                else -> false
            }
        }
    }

    private fun switchFragment(targetFragment: Fragment, title: String) {
        val transaction = supportFragmentManager.beginTransaction()

        activeFragment?.let {
            transaction.hide(it)
        }

        transaction.show(targetFragment)

        transaction.commit()

        activeFragment = targetFragment
        toolbarTitle.text = title

        if (supportFragmentManager.backStackEntryCount > 0) {
            for (i in 0 until supportFragmentManager.backStackEntryCount) {
                supportFragmentManager.popBackStackImmediate()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}