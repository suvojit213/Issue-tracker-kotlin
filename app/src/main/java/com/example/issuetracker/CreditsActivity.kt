package com.example.issuetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class CreditsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_credits)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button
        supportActionBar?.title = "Credits"
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}