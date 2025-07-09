package com.example.issuetracker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class FeedbackActivity : AppCompatActivity() {

    private var rating: Int = 0
    private lateinit var nameEditText: TextInputEditText
    private lateinit var feedbackEditText: TextInputEditText
    private lateinit var sendFeedbackButton: Button
    private lateinit var starRatingContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button
        supportActionBar?.title = "Feedback"

        nameEditText = findViewById(R.id.name_edit_text)
        feedbackEditText = findViewById(R.id.feedback_edit_text)
        sendFeedbackButton = findViewById(R.id.send_feedback_button)
        starRatingContainer = findViewById(R.id.star_rating_container)

        loadAdvisorName()
        setupStarRating()

        sendFeedbackButton.setOnClickListener {
            sendFeedback()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadAdvisorName() {
        val prefs = getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)
        val advisorName = prefs.getString("advisorName", "")
        nameEditText.setText(advisorName)
    }

    private fun setupStarRating() {
        starRatingContainer.removeAllViews()
        for (i in 1..5) {
            val star = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { weight = 1.0f }
                setImageResource(if (i <= rating) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
                setOnClickListener {
                    rating = i
                    updateStarRating()
                }
            }
            starRatingContainer.addView(star)
        }
    }

    private fun updateStarRating() {
        for (i in 0 until starRatingContainer.childCount) {
            val star = starRatingContainer.getChildAt(i) as ImageView
            star.setImageResource(if (i < rating) android.R.drawable.btn_star_big_on else android.R.drawable.btn_star_big_off)
        }
    }

    private fun sendFeedback() {
        val name = nameEditText.text.toString().trim()
        val feedback = feedbackEditText.text.toString().trim()

        if (name.isEmpty()) {
            nameEditText.error = "Please enter your name"
            return
        }

        val phoneNumber = "9234577086" // Your WhatsApp number

        val message = StringBuilder()
        message.append("*App Feedback*\n\n")
        message.append("*Rating:* $rating stars\n")
        message.append("*Feedback/Suggestions:*\n$feedback\n\n")
        message.append("*Name:* $name")

        val whatsappUri = Uri.parse("https://wa.me/$phoneNumber?text=${Uri.encode(message.toString())}")

        try {
            startActivity(Intent(Intent.ACTION_VIEW, whatsappUri))
            Toast.makeText(this, "Opening WhatsApp to send feedback.", Toast.LENGTH_SHORT).show()
            // Optionally clear fields after opening WhatsApp
            rating = 0
            nameEditText.setText("")
            feedbackEditText.setText("")
            updateStarRating()
        } catch (e: Exception) {
            Toast.makeText(this, "Could not launch WhatsApp. Please ensure WhatsApp is installed.", Toast.LENGTH_LONG).show()
        }
    }
}