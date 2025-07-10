package com.example.issuetracker

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.issuetracker.databinding.ActivityAdminSettingsBinding

class AdminSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminSettingsBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Admin Settings"

        sharedPreferences = getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)

        binding.viewRawSharedPreferencesTile.setOnClickListener { showRawSharedPreferences() }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun showRawSharedPreferences() {
        val allData = sharedPreferences.all.entries.joinToString("\n") { "${it.key}: ${it.value}" }

        val dialogBuilder = android.app.AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_raw_shared_preferences, null)
        dialogBuilder.setView(dialogView)

        val dataTextView = dialogView.findViewById<TextView>(R.id.raw_data_text_view)
        dataTextView.text = if (allData.isNotEmpty()) allData else "No data found."

        dialogBuilder.setPositiveButton("Close") { dialog, _ ->
            dialog.dismiss()
        }
        dialogBuilder.create().show()
    }
}

