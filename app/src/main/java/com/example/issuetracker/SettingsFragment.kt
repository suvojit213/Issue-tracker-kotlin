package com.example.issuetracker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.issuetracker.databinding.FragmentSettingsBinding
import com.google.android.material.snackbar.Snackbar

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSettingsTiles()
    }

    private fun setupSettingsTiles() {
        // General Settings
        binding.editProfileTile.setOnClickListener { navigateToEditProfile() }
        binding.adminSettingsTile.setOnClickListener { showAdminPasswordDialog() }

        // About Section
        binding.aboutAppTile.setOnClickListener { navigateToAboutApp() }
        binding.developerInfoTile.setOnClickListener { navigateToDeveloperInfo() }
        binding.viewSourceCodeTile.setOnClickListener { viewSourceCode() }
        binding.feedbackTile.setOnClickListener { navigateToFeedback() }
        binding.creditsTile.setOnClickListener { navigateToCredits() }
    }

    private fun navigateToEditProfile() {
        val intent = Intent(activity, EditProfileActivity::class.java)
        startActivity(intent)
    }

    private fun showAdminPasswordDialog() {
        val passwordEditText = EditText(requireContext()).apply {
            hint = "Enter password"
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Admin Access")
            .setView(passwordEditText)
            .setPositiveButton("Enter") { dialog, _ ->
                if (passwordEditText.text.toString() == "01082005") {
                    dialog.dismiss()
                    val intent = Intent(activity, AdminSettingsActivity::class.java)
                    startActivity(intent)
                } else {
                    showSnackbar("Incorrect password", true)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateToAboutApp() {
        val intent = Intent(activity, AboutAppActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToDeveloperInfo() {
        val intent = Intent(activity, DeveloperInfoActivity::class.java)
        startActivity(intent)
    }

    private fun viewSourceCode() {
        val url = "https://github.com/suvojit213/issue_tracker"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = android.net.Uri.parse(url)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            showSnackbar("Could not launch $url. Please ensure you have a web browser installed and an active internet connection.", true)
        }
    }

    private fun navigateToFeedback() {
        val intent = Intent(activity, FeedbackActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToCredits() {
        val intent = Intent(activity, CreditsActivity::class.java)
        startActivity(intent)
    }

    private fun showSnackbar(message: String, isError: Boolean) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(if (isError) R.color.red_500 else R.color.green_500, requireContext().theme))
            .setTextColor(resources.getColor(android.R.color.white, requireContext().theme))
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

