package com.example.issuetracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.issuetracker.utils.IssueParser
import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class HomeFragment : Fragment() {

    private lateinit var welcomeMessage: TextView
    private lateinit var crmIdText: TextView
    private lateinit var tlNameText: TextView
    private lateinit var advisorNameText: TextView
    private lateinit var totalIssuesText: TextView
    private lateinit var issuesTodayText: TextView
    private lateinit var analyticsSection: LinearLayout
    private lateinit var issueTypeBreakdownContainer: LinearLayout
    private lateinit var editProfileButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        welcomeMessage = view.findViewById(R.id.welcome_message)
        crmIdText = view.findViewById(R.id.crm_id_text)
        tlNameText = view.findViewById(R.id.tl_name_text)
        advisorNameText = view.findViewById(R.id.advisor_name_text)
        totalIssuesText = view.findViewById(R.id.total_issues_text)
        issuesTodayText = view.findViewById(R.id.issues_today_text)
        analyticsSection = view.findViewById(R.id.analytics_section)
        issueTypeBreakdownContainer = view.findViewById(R.id.issue_type_breakdown_container)
        editProfileButton = view.findViewById(R.id.edit_profile_button)

        editProfileButton.setOnClickListener {
            startActivity(Intent(context, EditProfileActivity::class.java))
        }

        loadUserData()
        loadAnalyticsData()

        return view
    }

    override fun onResume() {
        super.onResume()
        // Reload data when returning to this fragment (e.g., after editing profile)
        loadUserData()
        loadAnalyticsData()
    }

    private fun loadUserData() {
        val prefs = requireContext().getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)
        crmId = prefs.getString("crmId", "") ?: ""
        tlName = prefs.getString("tlName", "") ?: ""
        if (tlName == "Other") {
            tlName = prefs.getString("otherTlName", "") ?: ""
        }
        advisorName = prefs.getString("advisorName", "") ?: ""

        welcomeMessage.text = "Welcome Back, ${advisorName.split(" ").firstOrNull() ?: "User"}!"
        crmIdText.text = crmId.ifEmpty { "N/A" }
        tlNameText.text = tlName.ifEmpty { "N/A" }
        advisorNameText.text = advisorName.ifEmpty { "N/A" }
    }

    private fun loadAnalyticsData() {
        val prefs = requireContext().getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)
        val issueHistory = prefs.getStringSet("issueHistory", emptySet()) ?: emptySet()

        totalIssues = issueHistory.size
        issuesPerDay.clear()
        issueTypeBreakdown.clear()

        for (entry in issueHistory) {
            val parsedEntry = IssueParser.parseHistoryEntry(entry)
            val fillTime = parsedEntry["Fill Time"]
            val issueType = parsedEntry["Issue Explanation"]

            if (fillTime != null) {
                try {
                    val date = ZonedDateTime.parse(fillTime).toLocalDate()
                    val formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                    issuesPerDay[formattedDate] = (issuesPerDay[formattedDate] ?: 0) + 1
                } catch (e: Exception) {
                    // Handle parsing error
                }
            }

            if (issueType != null) {
                issueTypeBreakdown[issueType] = (issueTypeBreakdown[issueType] ?: 0) + 1
            }
        }

        totalIssuesText.text = totalIssues.toString()
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        issuesTodayText.text = (issuesPerDay[today] ?: 0).toString()

        if (totalIssues > 0) {
            analyticsSection.visibility = View.VISIBLE
            updateIssueTypeBreakdownUI()
        } else {
            analyticsSection.visibility = View.GONE
        }
    }

    private fun updateIssueTypeBreakdownUI() {
        issueTypeBreakdownContainer.removeAllViews()
        if (issueTypeBreakdown.isNotEmpty()) {
            for ((type, count) in issueTypeBreakdown) {
                val row = LayoutInflater.from(context).inflate(R.layout.item_issue_breakdown, issueTypeBreakdownContainer, false)
                row.findViewById<TextView>(R.id.issue_type_name).text = type
                row.findViewById<TextView>(R.id.issue_type_count).text = count.toString()
                issueTypeBreakdownContainer.addView(row)
            }
        }
    }
}