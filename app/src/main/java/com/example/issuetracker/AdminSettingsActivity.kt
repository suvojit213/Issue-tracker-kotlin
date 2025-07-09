package com.example.issuetracker

import android.content.Context
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AdminSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_settings)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button
        supportActionBar?.title = "Admin Settings"

        findViewById<LinearLayout>(R.id.setting_view_raw_prefs).setOnClickListener {
            showRawSharedPreferences()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun showRawSharedPreferences() {
        val prefs = getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)
        val allData = prefs.all.entries.joinToString("\n") { "${it.key}: ${it.value}" }

        AlertDialog.Builder(this)
            .setTitle("Raw SharedPreferences Data")
            .setMessage(if (allData.isNotEmpty()) allData else "No data found.")
            .setPositiveButton("Close") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}