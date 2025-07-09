package com.example.issuetracker

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.issuetracker.data.FullIssueDetails
import com.example.issuetracker.ui.IssueAdapter

class IssueTrackerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_issue_tracker, container, false)

        val recyclerView: RecyclerView = view.findViewById(R.id.issues_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(context)

        val dummyIssues = listOf(
            FullIssueDetails(
                advisorName = "John Doe",
                crmId = "CRM123",
                organization = "Example Org",
                issueType = "Bug in login",
                reason = "Users cannot log in with correct credentials.",
                issueRemarks = "Needs urgent fix.",
                fillTime = "2025-07-01T10:00:00Z",
                startTime = "2025-07-01T09:00:00Z",
                endTime = "2025-07-01T09:30:00Z",
                imagePaths = listOf()
            ),
            FullIssueDetails(
                advisorName = "Jane Smith",
                crmId = "CRM456",
                organization = "Another Org",
                issueType = "Feature Request: Dark Mode",
                reason = "Add a dark mode option to the app settings.",
                issueRemarks = null,
                fillTime = "2025-06-25T15:00:00Z",
                startTime = "2025-06-25T14:00:00Z",
                endTime = "2025-06-25T14:15:00Z",
                imagePaths = listOf()
            )
        )

        val adapter = IssueAdapter(dummyIssues) { issue ->
            val intent = Intent(context, IssueDetailActivity::class.java)
            intent.putExtra("full_issue_details", issue)
            startActivity(intent)
        }
        recyclerView.adapter = adapter

        return view
    }
}