package com.example.issuetracker

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import android.content.Context

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 750) // 750 milliseconds delay to match Flutter animation duration
    }

    private fun navigateToNextScreen() {
        val sharedPref = getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)
        val interactiveOnboardingComplete = sharedPref.getBoolean("interactive_onboarding_complete", false)
        val isSetupComplete = sharedPref.contains("crmId")

        val intent: Intent
        if (!isSetupComplete) {
            intent = Intent(this, InitialSetupActivity::class.java)
        } else {
            intent = Intent(this, IssueTrackerActivity::class.java) // MainAppScreen equivalent
        }
        startActivity(intent)
        finish()
    }
}
