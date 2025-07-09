package com.example.issuetracker

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.issuetracker.ui.HistoryAdapter
import com.example.issuetracker.utils.IssueParser
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar

class HistoryFragment : Fragment() {

    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var noResultsStateLayout: LinearLayout
    private lateinit var clearHistoryButton: ImageButton
    private lateinit var dateFilterButton: Button
    private lateinit var startTimeFilterButton: Button
    private lateinit var endTimeFilterButton: Button
    private lateinit var timeFilterLayout: LinearLayout
    private lateinit var clearTimeFilterButton: Button
    private lateinit var recordFirstIssueButton: Button

    private var issueHistory: MutableList<String> = mutableListOf()
    private var selectedDate: LocalDate? = null
    private var selectedStartTime: LocalTime? = null
    private var selectedEndTime: LocalTime? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)

        historyRecyclerView = view.findViewById(R.id.history_recycler_view)
        emptyStateLayout = view.findViewById(R.id.empty_state_layout)
        noResultsStateLayout = view.findViewById(R.id.no_results_state_layout)
        clearHistoryButton = view.findViewById(R.id.clear_history_button)
        dateFilterButton = view.findViewById(R.id.date_filter_button)
        startTimeFilterButton = view.findViewById(R.id.start_time_filter_button)
        endTimeFilterButton = view.findViewById(R.id.end_time_filter_button)
        timeFilterLayout = view.findViewById(R.id.time_filter_layout)
        clearTimeFilterButton = view.findViewById(R.id.clear_time_filter_button)
        recordFirstIssueButton = view.findViewById(R.id.record_first_issue_button)

        historyRecyclerView.layoutManager = LinearLayoutManager(context)
        historyAdapter = HistoryAdapter(issueHistory, requireContext()) { position ->
            confirmDelete(position)
        }
        historyRecyclerView.adapter = historyAdapter

        clearHistoryButton.setOnClickListener { clearHistory() }
        dateFilterButton.setOnClickListener { showDatePicker() }
        startTimeFilterButton.setOnClickListener { showTimePicker(true) }
        endTimeFilterButton.setOnClickListener { showTimePicker(false) }
        clearTimeFilterButton.setOnClickListener { clearTimeFilters() }
        recordFirstIssueButton.setOnClickListener { navigateToIssueTracker() }

        loadHistory()
        updateUI()

        return view
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
        updateUI()
    }

    private fun loadHistory() {
        val prefs = requireContext().getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)
        issueHistory.clear()
        issueHistory.addAll(prefs.getStringSet("issueHistory", emptySet())?.toList()?.reversed() ?: emptyList())
        filterHistory()
    }

    private fun filterHistory() {
        var filteredList = issueHistory.filter { entry ->
            val parsedEntry = IssueParser.parseHistoryEntry(entry)
            val fillTime = parsedEntry["Fill Time"]
            if (fillTime == null) return@filter false

            try {
                val entryDateTime = ZonedDateTime.parse(fillTime)
                val matchesDate = selectedDate == null ||
                        (entryDateTime.year == selectedDate?.year &&
                                entryDateTime.monthValue == selectedDate?.monthValue &&
                                entryDateTime.dayOfMonth == selectedDate?.dayOfMonth)

                val matchesTime = if (selectedDate != null && (selectedStartTime != null || selectedEndTime != null)) {
                    val issueStartTime = try { ZonedDateTime.parse(parsedEntry["Start Time"]).toLocalTime() } catch (e: Exception) { null }
                    val issueEndTime = try { ZonedDateTime.parse(parsedEntry["End Time"]).toLocalTime() } catch (e: Exception) { null }

                    var startMatch = true
                    if (selectedStartTime != null && issueStartTime != null) {
                        startMatch = issueStartTime.isAfter(selectedStartTime) || issueStartTime.equals(selectedStartTime)
                    }

                    var endMatch = true
                    if (selectedEndTime != null && issueEndTime != null) {
                        endMatch = issueEndTime.isBefore(selectedEndTime) || issueEndTime.equals(selectedEndTime)
                    }
                    startMatch && endMatch
                } else {
                    true
                }
                matchesDate && matchesTime
            } catch (e: DateTimeParseException) {
                false
            }
        }

        historyAdapter.updateData(filteredList)
        updateUIState(filteredList.isEmpty() && (selectedDate != null || selectedStartTime != null || selectedEndTime != null))
    }

    private fun updateUI() {
        if (issueHistory.isEmpty()) {
            emptyStateLayout.visibility = View.VISIBLE
            historyRecyclerView.visibility = View.GONE
            noResultsStateLayout.visibility = View.GONE
            clearHistoryButton.visibility = View.GONE
            timeFilterLayout.visibility = View.GONE
            clearTimeFilterButton.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            clearHistoryButton.visibility = View.VISIBLE
            if (selectedDate != null) {
                timeFilterLayout.visibility = View.VISIBLE
                clearTimeFilterButton.visibility = View.VISIBLE
            } else {
                timeFilterLayout.visibility = View.GONE
                clearTimeFilterButton.visibility = View.GONE
            }
            filterHistory()
        }
    }

    private fun updateUIState(noResults: Boolean) {
        if (noResults) {
            noResultsStateLayout.visibility = View.VISIBLE
            historyRecyclerView.visibility = View.GONE
        } else {
            noResultsStateLayout.visibility = View.GONE
            historyRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val year = selectedDate?.year ?: c.get(Calendar.YEAR)
        val month = selectedDate?.monthValue?.minus(1) ?: c.get(Calendar.MONTH)
        val day = selectedDate?.dayOfMonth ?: c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireContext(), { _, sYear, sMonth, sDay ->
            selectedDate = LocalDate.of(sYear, sMonth + 1, sDay)
            dateFilterButton.text = "${sDay}/${sMonth + 1}/${sYear}"
            selectedStartTime = null
            selectedEndTime = null
            startTimeFilterButton.text = "Start Time"
            endTimeFilterButton.text = "End Time"
            filterHistory()
            updateUI()
        }, year, month, day)
        dpd.show()
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val c = Calendar.getInstance()
        val hour = if (isStartTime) selectedStartTime?.hour ?: c.get(Calendar.HOUR_OF_DAY) else selectedEndTime?.hour ?: c.get(Calendar.HOUR_OF_DAY)
        val minute = if (isStartTime) selectedStartTime?.minute ?: c.get(Calendar.MINUTE) else selectedEndTime?.minute ?: c.get(Calendar.MINUTE)

        val tpd = TimePickerDialog(requireContext(), { _, sHour, sMinute ->
            val selectedTime = LocalTime.of(sHour, sMinute)
            if (isStartTime) {
                selectedStartTime = selectedTime
                startTimeFilterButton.text = selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
            } else {
                selectedEndTime = selectedTime
                endTimeFilterButton.text = selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
            }
            filterHistory()
            updateUI()
        }, hour, minute, false)
        tpd.show()
    }

    private fun clearTimeFilters() {
        selectedDate = null
        selectedStartTime = null
        selectedEndTime = null
        dateFilterButton.text = "Select Date"
        startTimeFilterButton.text = "Start Time"
        endTimeFilterButton.text = "End Time"
        filterHistory()
        updateUI()
    }

    private fun clearHistory() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear History")
            .setMessage("Are you sure you want to clear all issue history? This action cannot be undone.")
            .setPositiveButton("Clear") { dialog, _ ->
                val prefs = requireContext().getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)
                prefs.edit().remove("issueHistory").apply()
                issueHistory.clear()
                filterHistory()
                updateUI()
                Toast.makeText(context, "History cleared successfully", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun confirmDelete(position: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Entry")
            .setMessage("Are you sure you want to delete this history entry? This action cannot be undone.")
            .setPositiveButton("Delete") { dialog, _ ->
                deleteHistoryItem(position)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun deleteHistoryItem(position: Int) {
        val prefs = requireContext().getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)
        val currentHistory = prefs.getStringSet("issueHistory", emptySet())?.toMutableSet() ?: mutableSetOf()

        // The displayed list is reversed, so we need to adjust the index for the actual stored list
        val originalIndex = issueHistory.size - 1 - position
        if (originalIndex >= 0 && originalIndex < issueHistory.size) {
            val itemToDelete = issueHistory[position] // Get the item from the currently displayed list
            currentHistory.remove(itemToDelete) // Remove it from the original set
            prefs.edit().putStringSet("issueHistory", currentHistory).apply()
            issueHistory.removeAt(position)
            historyAdapter.notifyItemRemoved(position)
            updateUI()
            Toast.makeText(context, "Entry deleted successfully", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToIssueTracker() {
        val intent = Intent(activity, IssueTrackerActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}