package com.example.issuetracker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.issuetracker.databinding.FragmentHistoryBinding
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences
    private var issueHistory: MutableList<String> = mutableListOf()

    private var selectedDate: Calendar? = null
    private var selectedStartTime: Calendar? = null
    private var selectedEndTime: Calendar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)

        binding.historyRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadHistory()
        setupClickListeners()
        updateHistoryDisplay()
    }

    private fun loadHistory() {
        val historySet = sharedPreferences.getStringSet("issueHistory", emptySet())
        issueHistory = historySet?.toMutableList()?.reversed() ?: mutableListOf()
    }

    private fun setupClickListeners() {
        binding.clearHistoryButton.setOnClickListener { confirmClearHistory() }
        binding.dateFilterButton.setOnClickListener { showDatePicker() }
        binding.clearDateFilterButton.setOnClickListener { clearDateFilter() }
        binding.startTimeFilterButton.setOnClickListener { showTimePicker(true) }
        binding.endTimeFilterButton.setOnClickListener { showTimePicker(false) }
        binding.clearTimeFiltersButton.setOnClickListener { clearTimeFilters() }
    }

    private fun updateHistoryDisplay() {
        val filteredHistory = getFilteredHistory()
        if (filteredHistory.isEmpty()) {
            binding.historyContent.visibility = View.GONE
            binding.emptyState.visibility = View.VISIBLE
            binding.noResultsState.visibility = View.GONE
            binding.clearHistoryButton.visibility = View.GONE
        } else {
            binding.historyContent.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE
            binding.clearHistoryButton.visibility = View.VISIBLE
            if (filteredHistory.size == issueHistory.size) {
                binding.noResultsState.visibility = View.GONE
            } else {
                binding.noResultsState.visibility = View.VISIBLE
            }
            binding.historyRecyclerView.adapter = HistoryAdapter(filteredHistory) {
                // Handle item click to show details
                val intent = Intent(requireContext(), IssueDetailActivity::class.java)
                intent.putExtra("issue_details", it.toTypedArray())
                startActivity(intent)
            }
        }
    }

    private fun getFilteredHistory(): List<String> {
        var filteredList = issueHistory.filter { entry ->
            val parsedEntry = parseHistoryEntry(entry)
            val fillTimeStr = parsedEntry["Fill Time"]
            if (fillTimeStr == null) return@filter false

            try {
                val fillTime = Calendar.getInstance().apply { timeInMillis = fillTimeStr.toLong() }
                selectedDate == null ||
                (fillTime.get(Calendar.YEAR) == selectedDate?.get(Calendar.YEAR) &&
                 fillTime.get(Calendar.MONTH) == selectedDate?.get(Calendar.MONTH) &&
                 fillTime.get(Calendar.DAY_OF_MONTH) == selectedDate?.get(Calendar.DAY_OF_MONTH))
            } catch (e: Exception) {
                false
            }
        }

        if (selectedDate != null && (selectedStartTime != null || selectedEndTime != null)) {
            filteredList = filteredList.filter { entry ->
                val parsedEntry = parseHistoryEntry(entry)
                val startTimeStr = parsedEntry["Start Time"]
                val endTimeStr = parsedEntry["End Time"]

                if (startTimeStr == null || endTimeStr == null) return@filter false

                try {
                    val issueStartTime = Calendar.getInstance().apply { timeInMillis = startTimeStr.toLong() }
                    val issueEndTime = Calendar.getInstance().apply { timeInMillis = endTimeStr.toLong() }

                    // Apply date from selectedDate to issue times for comparison
                    issueStartTime.set(selectedDate!!.get(Calendar.YEAR), selectedDate!!.get(Calendar.MONTH), selectedDate!!.get(Calendar.DAY_OF_MONTH))
                    issueEndTime.set(selectedDate!!.get(Calendar.YEAR), selectedDate!!.get(Calendar.MONTH), selectedDate!!.get(Calendar.DAY_OF_MONTH))

                    var matchesStartTime = true
                    if (selectedStartTime != null) {
                        val selectedStartDateTime = Calendar.getInstance().apply {
                            set(selectedDate!!.get(Calendar.YEAR), selectedDate!!.get(Calendar.MONTH), selectedDate!!.get(Calendar.DAY_OF_MONTH))
                            set(Calendar.HOUR_OF_DAY, selectedStartTime!!.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, selectedStartTime!!.get(Calendar.MINUTE))
                        }
                        matchesStartTime = issueStartTime.timeInMillis >= selectedStartDateTime.timeInMillis &&
                                           issueStartTime.timeInMillis <= selectedStartDateTime.timeInMillis + (15 * 60 * 1000) // Within 15 minutes
                    }

                    var matchesEndTime = true
                    if (selectedEndTime != null) {
                        val selectedEndDateTime = Calendar.getInstance().apply {
                            set(selectedDate!!.get(Calendar.YEAR), selectedDate!!.get(Calendar.MONTH), selectedDate!!.get(Calendar.DAY_OF_MONTH))
                            set(Calendar.HOUR_OF_DAY, selectedEndTime!!.get(Calendar.HOUR_OF_DAY))
                            set(Calendar.MINUTE, selectedEndTime!!.get(Calendar.MINUTE))
                        }
                        matchesEndTime = issueEndTime.timeInMillis <= selectedEndDateTime.timeInMillis &&
                                         issueEndTime.timeInMillis >= selectedEndDateTime.timeInMillis - (15 * 60 * 1000) // Within 15 minutes
                    }
                    matchesStartTime && matchesEndTime
                } catch (e: Exception) {
                    false
                }
            }
        }
        return filteredList
    }

    private fun parseHistoryEntry(entry: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        entry.split(", ").forEach { part ->
            val colonIndex = part.indexOf(": ")
            if (colonIndex != -1) {
                val key = part.substring(0, colonIndex).trim()
                val value = part.substring(colonIndex + 2).trim()
                map[key] = value
            }
        }
        return map
    }

    private fun confirmClearHistory() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear History")
            .setMessage("Are you sure you want to clear all issue history? This action cannot be undone.")
            .setPositiveButton("Clear") { dialog, _ ->
                sharedPreferences.edit().remove("issueHistory").apply()
                issueHistory.clear()
                updateHistoryDisplay()
                showSnackbar("History cleared successfully", false)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDatePicker() {
        val c = selectedDate ?: Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(requireContext(), {
                _, selectedYear, selectedMonth, selectedDayOfMonth ->
            selectedDate = Calendar.getInstance().apply {
                set(selectedYear, selectedMonth, selectedDayOfMonth)
            }
            binding.dateFilterButton.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(selectedDate!!.time)
            binding.clearDateFilterButton.visibility = View.VISIBLE
            updateHistoryDisplay()
        }, year, month, day)
        dpd.show()
    }

    private fun clearDateFilter() {
        selectedDate = null
        selectedStartTime = null
        selectedEndTime = null
        binding.dateFilterButton.text = "Select Date"
        binding.clearDateFilterButton.visibility = View.GONE
        binding.startTimeFilterButton.text = "Start Time"
        binding.endTimeFilterButton.text = "End Time"
        binding.clearTimeFiltersButton.visibility = View.GONE
        updateHistoryDisplay()
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val c = if (isStartTime) selectedStartTime ?: Calendar.getInstance() else selectedEndTime ?: Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val tpd = TimePickerDialog(requireContext(), {
                _, selectedHour, selectedMinute ->
            val newTime = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, selectedHour)
                set(Calendar.MINUTE, selectedMinute)
            }
            if (isStartTime) {
                selectedStartTime = newTime
                binding.startTimeFilterButton.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(newTime.time)
            } else {
                selectedEndTime = newTime
                binding.endTimeFilterButton.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(newTime.time)
            }
            binding.clearTimeFiltersButton.visibility = View.VISIBLE
            updateHistoryDisplay()
        }, hour, minute, false)
        tpd.show()
    }

    private fun clearTimeFilters() {
        selectedStartTime = null
        selectedEndTime = null
        binding.startTimeFilterButton.text = "Start Time"
        binding.endTimeFilterButton.text = "End Time"
        binding.clearTimeFiltersButton.visibility = View.GONE
        updateHistoryDisplay()
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

    inner class HistoryAdapter(private val historyList: List<String>, private val onItemClick: (Map<String, String>) -> Unit) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_entry, parent, false)
            return HistoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            val entry = historyList[position]
            val parsedEntry = parseHistoryEntry(entry)
            holder.bind(parsedEntry, position)
        }

        override fun getItemCount(): Int = historyList.size

        inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val issueNumberTextView: TextView = itemView.findViewById(R.id.issue_number_text_view)
            private val issueTypeTextView: TextView = itemView.findViewById(R.id.issue_type_text_view)
            private val reasonTextView: TextView = itemView.findViewById(R.id.reason_text_view)
            private val remarksTextView: TextView = itemView.findViewById(R.id.remarks_text_view)
            private val startTimeTextView: TextView = itemView.findViewById(R.id.start_time_text_view)
            private val endTimeTextView: TextView = itemView.findViewById(R.id.end_time_text_view)
            private val durationTextView: TextView = itemView.findViewById(R.id.duration_text_view)
            private val shareButton: ImageView = itemView.findViewById(R.id.share_button)
            private val deleteButton: ImageView = itemView.findViewById(R.id.delete_button)

            fun bind(parsedEntry: Map<String, String>, position: Int) {
                issueNumberTextView.text = "Issue #${historyList.size - position}"
                issueTypeTextView.text = parsedEntry["Issue Explanation"]
                reasonTextView.text = parsedEntry["Reason"]
                val remarks = parsedEntry["Issue Remarks"]
                if (!remarks.isNullOrEmpty()) {
                    remarksTextView.visibility = View.VISIBLE
                    remarksTextView.text = remarks
                } else {
                    remarksTextView.visibility = View.GONE
                }

                val startTimeIso = parsedEntry["Start Time"]
                val endTimeIso = parsedEntry["End Time"]

                if (startTimeIso != null) {
                    startTimeTextView.text = "Start: ${formatTime(startTimeIso)}"
                }
                if (endTimeIso != null) {
                    endTimeTextView.text = "End: ${formatTime(endTimeIso)}"
                }

                if (startTimeIso != null && endTimeIso != null) {
                    durationTextView.text = "Duration: ${formatDuration(startTimeIso, endTimeIso)}"
                }

                itemView.setOnClickListener { onItemClick(parsedEntry) }
                shareButton.setOnClickListener { shareIssue(parsedEntry) }
                deleteButton.setOnClickListener { confirmDeleteItem(position) }
            }

            private fun formatTime(timeInMillis: String): String {
                return try {
                    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    sdf.format(Date(timeInMillis.toLong()))
                } catch (e: Exception) {
                    "N/A"
                }
            }

            private fun formatDuration(startTimeIso: String, endTimeIso: String): String {
                return try {
                    val start = startTimeIso.toLong()
                    val end = endTimeIso.toLong()
                    val durationMillis = end - start

                    val hours = durationMillis / (1000 * 60 * 60)
                    val minutes = (durationMillis % (1000 * 60 * 60)) / (1000 * 60)
                    val seconds = (durationMillis % (1000 * 60)) / 1000

                    when {
                        hours > 0 -> String.format("%dh %dm", hours, minutes)
                        minutes > 0 -> String.format("%dm", minutes)
                        else -> String.format("%ds", seconds)
                    }
                } catch (e: Exception) {
                    "N/A"
                }
            }

            private fun shareIssue(parsedEntry: Map<String, String>) {
                val message = buildShareMessage(parsedEntry)
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_TEXT, message)
                startActivity(Intent.createChooser(shareIntent, "Share Issue"))
            }

            private fun buildShareMessage(parsedEntry: Map<String, String>): String {
                val startTimeIso = parsedEntry["Start Time"] ?: ""
                val endTimeIso = parsedEntry["End Time"] ?: ""
                val fillTimeIso = parsedEntry["Fill Time"] ?: ""

                val startDate = if (startTimeIso.isNotEmpty()) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(startTimeIso.toLong())) else "N/A"
                val endDate = if (endTimeIso.isNotEmpty()) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(endTimeIso.toLong())) else "N/A"
                val fillDate = if (fillTimeIso.isNotEmpty()) SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(fillTimeIso.toLong())) else "N/A"

                return "*Issue Report*\n\n" +
                       "*Advisor Name:* ${parsedEntry["Advisor Name"] ?: "N/A"}\n" +
                       "*CRM ID:* ${parsedEntry["CRM ID"] ?: "N/A"}\n" +
                       "*Organization:* ${parsedEntry["Organization"] ?: "N/A"}\n\n" +
                       "*Issue:* ${parsedEntry["Issue Explanation"] ?: "N/A"}\n" +
                       "*Reason:* ${parsedEntry["Reason"] ?: "N/A"}\n\n" +
                       "*Start Time:* ${formatTime(startTimeIso)} on $startDate\n" +
                       "*End Time:* ${formatTime(endTimeIso)} on $endDate\n" +
                       "*Duration:* ${formatDuration(startTimeIso, endTimeIso)}\n" +
                       "*Fill Time:* ${formatTime(fillTimeIso)} on $fillDate\n" +
                       (if (!parsedEntry["Issue Remarks"].isNullOrEmpty()) "*Remarks:* ${parsedEntry["Issue Remarks"]}\n" else "") +
                       "\nThis report was generated from the Issue Tracker App."
            }

            private fun confirmDeleteItem(position: Int) {
                AlertDialog.Builder(itemView.context)
                    .setTitle("Delete Entry")
                    .setMessage("Are you sure you want to delete this history entry? This action cannot be undone.")
                    .setPositiveButton("Delete") { dialog, _ ->
                        deleteHistoryItem(position)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }

            private fun deleteHistoryItem(position: Int) {
                val originalIndex = issueHistory.size - 1 - position // Adjust for reversed list
                if (originalIndex >= 0 && originalIndex < issueHistory.size) {
                    val itemToRemove = issueHistory[originalIndex]
                    issueHistory.removeAt(originalIndex)
                    sharedPreferences.edit().putStringSet("issueHistory", issueHistory.toSet()).apply()
                    updateHistoryDisplay()
                    showSnackbar("Entry deleted successfully", false)
                }
            }

            private fun showSnackbar(message: String, isError: Boolean) {
                Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(resources.getColor(if (isError) R.color.red_500 else R.color.green_500, requireContext().theme))
                    .setTextColor(resources.getColor(android.R.color.white, requireContext().theme))
                    .show()
            }
        }
    }
}

