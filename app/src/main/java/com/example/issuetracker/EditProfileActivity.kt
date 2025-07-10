
package com.example.issuetracker

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class EditProfileActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var crmIdEditText: EditText
    private lateinit var tlNameSpinner: Spinner
    private lateinit var otherTlNameEditText: EditText
    private lateinit var advisorNameEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        sharedPreferences = getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)

        crmIdEditText = findViewById(R.id.crm_id_edit_text)
        tlNameSpinner = findViewById(R.id.tl_name_spinner)
        otherTlNameEditText = findViewById(R.id.other_tl_name_edit_text)
        advisorNameEditText = findViewById(R.id.advisor_name_edit_text)
        saveButton = findViewById(R.id.save_button)

        setupSpinner()
        loadProfileData()

        saveButton.setOnClickListener {
            saveProfileData()
        }
    }

    private fun setupSpinner() {
        val tlNames = arrayOf("TL1", "TL2", "TL3", "Other") // Example TL names
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tlNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tlNameSpinner.adapter = adapter

        // Handle "Other" selection visibility
        tlNameSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                if (parent.getItemAtPosition(position).toString() == "Other") {
                    otherTlNameEditText.visibility = View.VISIBLE
                } else {
                    otherTlNameEditText.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                // Do nothing
            }
        }
    }

    private fun loadProfileData() {
        crmIdEditText.setText(sharedPreferences.getString("crmId", ""))
        val savedTlName = sharedPreferences.getString("tlName", "")
        val tlNames = (tlNameSpinner.adapter as ArrayAdapter<String>).getPosition(savedTlName)
        tlNameSpinner.setSelection(tlNames)

        if (savedTlName == "Other") {
            otherTlNameEditText.setText(sharedPreferences.getString("otherTlName", ""))
            otherTlNameEditText.visibility = View.VISIBLE
        }

        advisorNameEditText.setText(sharedPreferences.getString("advisorName", ""))
    }

    private fun saveProfileData() {
        with(sharedPreferences.edit()) {
            putString("crmId", crmIdEditText.text.toString())
            val selectedTl = tlNameSpinner.selectedItem.toString()
            putString("tlName", selectedTl)
            if (selectedTl == "Other") {
                putString("otherTlName", otherTlNameEditText.text.toString())
            } else {
                remove("otherTlName")
            }
            putString("advisorName", advisorNameEditText.text.toString())
            apply()
        }
        Toast.makeText(this, "Profile Saved!", Toast.LENGTH_SHORT).show()
        finish()
    }
}

