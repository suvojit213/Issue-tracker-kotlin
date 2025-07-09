package com.example.issuetracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.regex.Pattern

class InitialSetupActivity : AppCompatActivity() {

    private lateinit var crmIdEditText: TextInputEditText
    private lateinit var advisorNameEditText: TextInputEditText
    private lateinit var tlNameSpinner: Spinner
    private lateinit var otherTlNameLayout: TextInputLayout
    private lateinit var otherTlNameEditText: TextInputEditText
    private lateinit var organizationSpinner: Spinner
    private lateinit var completeSetupButton: Button

    private var selectedTlName: String = "Manish Kumar"
    private var showOtherTlNameField: Boolean = false
    private var selectedOrganization: String = "DISH"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_setup)

        crmIdEditText = findViewById(R.id.crm_id_edit_text)
        advisorNameEditText = findViewById(R.id.advisor_name_edit_text)
        tlNameSpinner = findViewById(R.id.tl_name_spinner)
        otherTlNameLayout = findViewById(R.id.other_tl_name_layout)
        otherTlNameEditText = findViewById(R.id.other_tl_name_edit_text)
        organizationSpinner = findViewById(R.id.organization_spinner)
        completeSetupButton = findViewById(R.id.complete_setup_button)

        setupSpinners()
        loadSavedData()

        completeSetupButton.setOnClickListener {
            saveData()
        }
    }

    private fun setupSpinners() {
        val tlOptions = listOf(
            "Manish Kumar", "Aniket", "Imran Khan", "Ravi", "Gajendra",
            "Suyash Upadhyay", "Randhir Kumar", "Subham Kumar", "Karan",
            "Rohit", "Shilpa", "Vipin", "Sonu", "Other"
        )
        val tlAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tlOptions)
        tlAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tlNameSpinner.adapter = tlAdapter

        tlNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTlName = parent.getItemAtPosition(position).toString()
                showOtherTlNameField = (selectedTlName == "Other")
                otherTlNameLayout.visibility = if (showOtherTlNameField) View.VISIBLE else View.GONE
                if (!showOtherTlNameField) {
                    otherTlNameEditText.setText("")
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val orgOptions = listOf("DISH", "D2H")
        val orgAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, orgOptions)
        orgAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        organizationSpinner.adapter = orgAdapter

        organizationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedOrganization = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun loadSavedData() {
        val prefs = getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)
        crmIdEditText.setText(prefs.getString("crmId", ""))
        advisorNameEditText.setText(prefs.getString("advisorName", ""))

        val savedTlName = prefs.getString("tlName", "Manish Kumar")
        val tlOptions = (tlNameSpinner.adapter as ArrayAdapter<String>).getPosition(savedTlName)
        tlNameSpinner.setSelection(tlOptions)

        if (savedTlName == "Other") {
            otherTlNameEditText.setText(prefs.getString("otherTlName", ""))
        }

        val savedOrganization = prefs.getString("organization", "DISH")
        val orgOptions = (organizationSpinner.adapter as ArrayAdapter<String>).getPosition(savedOrganization)
        organizationSpinner.setSelection(orgOptions)
    }

    private fun saveData() {
        val crmId = crmIdEditText.text.toString().trim()
        val advisorName = advisorNameEditText.text.toString().trim()
        val otherTlName = otherTlNameEditText.text.toString().trim()

        if (advisorName.isEmpty() || (selectedTlName == "Other" && otherTlName.isEmpty())) {
            showErrorToast("Please fill in all required fields")
            return
        }

        if (crmId.isEmpty() || !Pattern.matches("^[0-9]+$", crmId)) {
            showErrorToast("CRM ID must contain only digits")
            return
        }

        if (!Pattern.matches("^[a-zA-Z ]+$", advisorName)) {
            showErrorToast("Advisor Name must contain only alphabets")
            return
        }

        val prefs = getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString("crmId", crmId)
            putString("advisorName", advisorName)
            putString("tlName", selectedTlName)
            if (showOtherTlNameField) {
                putString("otherTlName", otherTlName)
            } else {
                remove("otherTlName")
            }
            putString("organization", selectedOrganization)
            apply()
        }

        val intent = Intent(this, IssueTrackerActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun showErrorToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}