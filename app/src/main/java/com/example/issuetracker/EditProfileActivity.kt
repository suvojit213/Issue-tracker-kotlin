package com.example.issuetracker

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class EditProfileActivity : AppCompatActivity() {

    private lateinit var crmIdEditText: TextInputEditText
    private lateinit var tlNameEditText: TextInputEditText
    private lateinit var advisorNameEditText: TextInputEditText
    private lateinit var saveChangesButton: Button
    private lateinit var backButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        crmIdEditText = findViewById(R.id.crm_id_edit_text)
        tlNameEditText = findViewById(R.id.tl_name_edit_text)
        advisorNameEditText = findViewById(R.id.advisor_name_edit_text)
        saveChangesButton = findViewById(R.id.save_changes_button)
        backButton = findViewById(R.id.back_button)

        loadUserData()

        saveChangesButton.setOnClickListener {
            saveUserData()
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)
        crmIdEditText.setText(prefs.getString("crmId", ""))
        tlNameEditText.setText(prefs.getString("tlName", ""))
        advisorNameEditText.setText(prefs.getString("advisorName", ""))
    }

    private fun saveUserData() {
        val prefs = getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString("crmId", crmIdEditText.text.toString())
            putString("tlName", tlNameEditText.text.toString())
            putString("advisorName", advisorNameEditText.text.toString())
            apply()
        }
        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        finish() // Go back to previous activity
    }
}