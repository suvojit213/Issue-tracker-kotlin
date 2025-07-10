package com.example.issuetracker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.example.issuetracker.databinding.FragmentDashboardBinding
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    private var crmId: String = ""
    private var tlName: String = ""
    private var advisorName: String = ""
    private var totalIssues: Int = 0
    private var issuesPerDay: MutableMap<String, Int> = mutableMapOf()
    private var issueTypeBreakdown: MutableMap<String, Int> = mutableMapOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)

        loadUserData()
        loadAnalyticsData()

        binding.editProfileButton.setOnClickListener {
            // Navigate to EditProfileActivity (create this activity later)
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.infoButton.setOnClickListener {
            // Navigate to DeveloperInfoActivity (create this activity later)
            val intent = Intent(requireContext(), DeveloperInfoActivity::class.java)
            startActivity(intent)
        }

        binding.fabFillIssue.setOnClickListener {
            // Navigate to IssueTrackerActivity (create this activity later)
            val intent = Intent(requireContext(), IssueTrackerActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadUserData() {
        crmId = sharedPreferences.getString("crmId", "") ?: ""
        tlName = sharedPreferences.getString("tlName", "") ?: ""
        if (tlName == "Other") {
            tlName = sharedPreferences.getString("otherTlName", "") ?: ""
        }
        advisorName = sharedPreferences.getString("advisorName", "") ?: ""

        binding.welcomeText.text = "Welcome Back, ${advisorName.split(" ").firstOrNull() ?: "User"}!"
        binding.crmIdValue.text = if (crmId.isNotEmpty()) crmId else "Not set"
        binding.tlNameValue.text = if (tlName.isNotEmpty()) tlName else "Not set"
        binding.advisorNameValue.text = if (advisorName.isNotEmpty()) advisorName else "Not set"
    }

    private fun loadAnalyticsData() {
        val issueHistory = sharedPreferences.getStringSet("issueHistory", emptySet()) ?: emptySet()

        totalIssues = issueHistory.size
        issuesPerDay.clear()
        issueTypeBreakdown.clear()

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        for (entry in issueHistory) {
            val parsedEntry = parseHistoryEntry(entry)
            val fillTime = parsedEntry["Fill Time"]
            val issueType = parsedEntry["Issue Explanation"]

            if (fillTime != null) {
                try {
                    val date = dateFormat.format(Date(fillTime.toLong())) // Assuming fillTime is a timestamp
                    issuesPerDay[date] = (issuesPerDay[date] ?: 0) + 1
                } catch (e: Exception) {
                    // Handle parsing error if fillTime is not a valid timestamp
                }
            }

            if (issueType != null) {
                issueTypeBreakdown[issueType] = (issueTypeBreakdown[issueType] ?: 0) + 1
            }
        }

        updateAnalyticsUI()
    }

    private fun updateAnalyticsUI() {
        if (totalIssues > 0) {
            binding.analyticsSection.visibility = View.VISIBLE
            binding.totalIssuesValue.text = totalIssues.toString()

            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            binding.issuesTodayValue.text = (issuesPerDay[today] ?: 0).toString()

            if (issueTypeBreakdown.isNotEmpty()) {
                binding.issueTypeBreakdownCard.visibility = View.VISIBLE
                binding.issueTypeBreakdownContainer.removeAllViews()
                for ((type, count) in issueTypeBreakdown) {
                    val row = LayoutInflater.from(requireContext()).inflate(R.layout.item_issue_type_breakdown, binding.issueTypeBreakdownContainer, false)
                    row.findViewById<TextView>(R.id.issue_type_name).text = type
                    row.findViewById<TextView>(R.id.issue_type_count).text = count.toString()
                    binding.issueTypeBreakdownContainer.addView(row)
                }
            }
        } else {
            binding.analyticsSection.visibility = View.GONE
        }
    }

    // Placeholder for parseHistoryEntry - implement actual parsing logic here
    private fun parseHistoryEntry(entry: String): Map<String, String> {
        // This is a simplified placeholder. You'll need to implement the actual parsing
        // logic based on how your Flutter app's issue_parser.dart works.
        // For example, if it's a comma-separated string: "key1:value1,key2:value2"
        val map = mutableMapOf<String, String>()
        val parts = entry.split(",")
        for (part in parts) {
            val keyValue = part.split(":", 2)
            if (keyValue.size == 2) {
                map[keyValue[0].trim()] = keyValue[1].trim()
            }
        }
        return map
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

