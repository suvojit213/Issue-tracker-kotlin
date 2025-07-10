package com.example.issuetracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        val sharedPreferences = getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)
        val isSetupComplete = sharedPreferences.contains("crmId") && sharedPreferences.contains("advisorName")

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = if (isSetupComplete) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, InitialSetupActivity::class.java)
            }
            startActivity(intent)
            finish()
        }, 2000) // 2 seconds delay
    }
}

