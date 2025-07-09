package com.example.issuetracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        view.findViewById<LinearLayout>(R.id.setting_edit_profile).setOnClickListener {
            startActivity(Intent(context, EditProfileActivity::class.java))
        }

        view.findViewById<LinearLayout>(R.id.setting_admin_settings).setOnClickListener {
            showAdminPasswordDialog()
        }

        view.findViewById<LinearLayout>(R.id.setting_about_app).setOnClickListener {
            startActivity(Intent(context, AboutAppActivity::class.java))
        }

        view.findViewById<LinearLayout>(R.id.setting_developer_info).setOnClickListener {
            startActivity(Intent(context, DeveloperInfoActivity::class.java))
        }

        view.findViewById<LinearLayout>(R.id.setting_view_source_code).setOnClickListener {
            val url = "https://github.com/suvojit213/issue_tracker"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            try {
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not launch browser. Please ensure you have a web browser installed.", Toast.LENGTH_LONG).show()
            }
        }

        view.findViewById<LinearLayout>(R.id.setting_feedback).setOnClickListener {
            startActivity(Intent(context, FeedbackActivity::class.java))
        }

        view.findViewById<LinearLayout>(R.id.setting_credits).setOnClickListener {
            startActivity(Intent(context, CreditsActivity::class.java))
        }

        return view
    }

    private fun showAdminPasswordDialog() {
        val passwordEditText = EditText(context)
        passwordEditText.hint = "Enter password"
        passwordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        AlertDialog.Builder(requireContext())
            .setTitle("Admin Access")
            .setView(passwordEditText)
            .setPositiveButton("Enter") {
                dialog, _ ->
                val password = passwordEditText.text.toString()
                if (password == "01082005") {
                    dialog.dismiss()
                    startActivity(Intent(context, AdminSettingsActivity::class.java))
                } else {
                    Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") {
                dialog, _ -> dialog.cancel()
            }
            .show()
    }
}