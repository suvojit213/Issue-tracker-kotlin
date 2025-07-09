package com.example.issuetracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DeveloperInfoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer_info)

        val backButton: ImageButton = findViewById(R.id.back_button)
        backButton.setOnClickListener { finish() }

        // Populate skill tags programmatically
        val skillTagsContainer: LinearLayout = findViewById(R.id.skill_tags_container)
        val skills = listOf(
            Pair("Flutter", "#1E3A8A"),
            Pair("Dart", "#059669"),
            Pair("Mobile Development", "#EF4444"),
            Pair("UI/UX Design", "#8B5CF6")
        )

        for (skill in skills) {
            val skillTag = LayoutInflater.from(this).inflate(R.layout.item_skill_tag, skillTagsContainer, false) as TextView
            skillTag.text = skill.first
            // Set background color dynamically (requires a drawable with a solid color)
            // For simplicity, I'll just set text color here, or you can create dynamic drawables
            // skillTag.setBackgroundColor(android.graphics.Color.parseColor(skill.second))
            skillTagsContainer.addView(skillTag)
        }

        // Contact items
        findViewById<LinearLayout>(R.id.contact_email).setOnClickListener {
            launchUrl("mailto:suvojitsengupta21@gmail.com")
        }

        findViewById<LinearLayout>(R.id.contact_github).setOnClickListener {
            launchUrl("https://github.com/suvojit213")
        }
    }

    private fun launchUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not launch $url. Please ensure you have a web browser installed.", Toast.LENGTH_LONG).show()
        }
    }
}