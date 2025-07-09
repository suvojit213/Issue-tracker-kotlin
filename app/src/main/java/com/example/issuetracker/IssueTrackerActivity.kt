package com.example.issuetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class IssueTrackerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_tracker)

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        bottomNavigationView.setOnItemSelectedListener { item ->
            var selectedFragment: Fragment? = null
            when (item.itemId) {
                R.id.navigation_home -> selectedFragment = HomeFragment()
                R.id.navigation_tracker -> selectedFragment = IssueTrackerFragment()
                R.id.navigation_history -> selectedFragment = HistoryFragment()
                R.id.navigation_settings -> selectedFragment = SettingsFragment()
            }
            selectedFragment?.let { loadFragment(it) }
            true
        }

        // Load the default fragment (HomeFragment)
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
