package com.example.issuetracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.issuetracker.databinding.ActivityFeedbackBinding
import com.google.android.material.snackbar.Snackbar

class FeedbackActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedbackBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var rating: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Feedback"

        sharedPreferences = getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)

        loadAdvisorName()
        setupRatingStars()
        binding.sendFeedbackButton.setOnClickListener { sendFeedback() }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun loadAdvisorName() {
        val advisorName = sharedPreferences.getString("advisorName", "")
        binding.nameEditText.setText(advisorName)
    }

    private fun setupRatingStars() {
        val stars = listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5)
        stars.forEachIndexed { index, star ->
            star.setOnClickListener { 
                rating = index + 1
                updateStarDisplay(stars)
            }
        }
        updateStarDisplay(stars)
    }

    private fun updateStarDisplay(stars: List<ImageView>) {
        stars.forEachIndexed { index, star ->
            if (index < rating) {
                star.setImageResource(R.drawable.ic_star_rounded)
            } else {
                star.setImageResource(R.drawable.ic_star_border_rounded)
            }
        }
    }

    private fun sendFeedback() {
        val name = binding.nameEditText.text.toString()
        val feedback = binding.feedbackEditText.text.toString()

        if (name.isEmpty()) {
            showSnackbar("Please enter your name", true)
            return
        }

        val phoneNumber = "9234577086" // Your WhatsApp number

        val message = "*App Feedback*\n\n" +
                "*Rating:* $rating stars\n" +
                "*Feedback/Suggestions:*\n$feedback\n\n" +
                "*Name:* $name"

        val whatsappUri = Uri.parse("https://wa.me/$phoneNumber?text=${Uri.encodeComponent(message)}")

        try {
            startActivity(Intent(Intent.ACTION_VIEW, whatsappUri))
            showSnackbar("Opening WhatsApp to send feedback.", false)
            // Optionally clear fields after opening WhatsApp
            rating = 0
            binding.nameEditText.setText("")
            binding.feedbackEditText.setText("")
            updateStarDisplay(listOf(binding.star1, binding.star2, binding.star3, binding.star4, binding.star5))
        } catch (e: Exception) {
            showSnackbar("Could not launch WhatsApp. Please ensure WhatsApp is installed.", true)
        }
    }

    private fun showSnackbar(message: String, isError: Boolean) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(if (isError) R.color.red_500 else R.color.green_500, theme))
            .setTextColor(resources.getColor(android.R.color.white, theme))
            .show()
    }
}

