package com.example.issuetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.example.issuetracker.R

class IssueDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_detail)

        val issueTitle: TextView = findViewById(R.id.detail_issue_title)
        val issueDescription: TextView = findViewById(R.id.detail_issue_description)
        val issueStatus: TextView = findViewById(R.id.detail_issue_status)
        val issueDate: TextView = findViewById(R.id.detail_issue_date)

        val title = intent.getStringExtra("issue_title")
        val description = intent.getStringExtra("issue_description")
        val status = intent.getStringExtra("issue_status")
        val date = intent.getStringExtra("issue_date")

        issueTitle.text = title
        issueDescription.text = description
        issueStatus.text = status
        issueDate.text = date
    }
}
