package com.example.issuetracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.issuetracker.databinding.ActivityInitialSetupBinding
import com.google.android.material.snackbar.Snackbar
import com.rizafu.coachmark.CoachMark

class InitialSetupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityInitialSetupBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInitialSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)

        setupSpinners()
        setupSaveButton()
        showCoachMarks()
    }

    private fun setupSpinners() {
        val tlNames = resources.getStringArray(R.array.tl_names)
        val tlAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tlNames)
        tlAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.tlNameSpinner.adapter = tlAdapter

        val organizations = resources.getStringArray(R.array.organizations)
        val orgAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, organizations)
        orgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.organizationSpinner.adapter = orgAdapter

        binding.tlNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (parent?.getItemAtPosition(position).toString() == "Add New") {
                    binding.newTlNameLayout.visibility = View.VISIBLE
                } else {
                    binding.newTlNameLayout.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupSaveButton() {
        binding.saveButton.setOnClickListener { saveInitialSetup() }
    }

    private fun saveInitialSetup() {
        val advisorName = binding.advisorNameEditText.text.toString()
        val crmId = binding.crmIdEditText.text.toString()
        val tlName = if (binding.tlNameSpinner.selectedItem.toString() == "Add New") {
            binding.newTlNameEditText.text.toString()
        } else {
            binding.tlNameSpinner.selectedItem.toString()
        }
        val organization = binding.organizationSpinner.selectedItem.toString()

        if (advisorName.isEmpty() || crmId.isEmpty() || tlName.isEmpty()) {
            showSnackbar("Please fill all fields", true)
            return
        }

        with(sharedPreferences.edit()) {
            putString("advisorName", advisorName)
            putString("crmId", crmId)
            putString("tlName", tlName)
            putString("organization", organization)
            putBoolean("initialSetupDone", true)
            apply()
        }

        showSnackbar("Setup saved successfully!", false)
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showSnackbar(message: String, isError: Boolean) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(if (isError) R.color.red_500 else R.color.green_500, theme))
            .setTextColor(resources.getColor(android.R.color.white, theme))
            .show()
    }

    private fun showCoachMarks() {
        val initialSetupDone = sharedPreferences.getBoolean("initialSetupDone", false)
        if (!initialSetupDone) {
            CoachMark.Builder(this)
                .addTarget(binding.advisorNameEditText, "Advisor Name", "Enter your full name here.")
                .addTarget(binding.crmIdEditText, "CRM ID", "Enter your CRM ID here.")
                .addTarget(binding.tlNameSpinner, "Team Leader", "Select your Team Leader or add a new one.")
                .addTarget(binding.organizationSpinner, "Organization", "Select your organization (DISH/D2H).")
                .addTarget(binding.saveButton, "Save Setup", "Click here to save your initial setup.")
                .setDismissible(true)
                .setOverlayColor(R.color.overlay_color)
                .build()
                .start()
        }
    }
}

