package com.example.issuetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.issuetracker.data.Issue
import com.example.issuetracker.ui.IssueAdapter

class IssueTrackerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_tracker)
        val recyclerView: RecyclerView = findViewById(R.id.issues_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val dummyIssues = listOf(
            Issue("1", "Bug in login", "Users cannot log in with correct credentials.", "Open", "2025-07-01"),
            Issue("2", "Feature Request: Dark Mode", "Add a dark mode option to the app settings.", "Closed", "2025-06-25"),
            Issue("3", "UI Glitch on Profile", "Profile picture is sometimes distorted.", "In Progress", "2025-07-05")
        )

        val adapter = IssueAdapter(dummyIssues)
        recyclerView.adapter = adapter
    }
}
