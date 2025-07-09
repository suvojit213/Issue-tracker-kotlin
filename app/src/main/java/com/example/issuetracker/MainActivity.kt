package com.example.issuetracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(this, SplashScreenActivity::class.java)
        startActivity(intent)
        finish()
        
    }
}
