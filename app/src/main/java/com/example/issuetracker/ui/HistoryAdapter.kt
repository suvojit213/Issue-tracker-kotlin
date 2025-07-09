package com.example.issuetracker.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import com.example.issuetracker.IssueDetailActivity
import com.example.issuetracker.R
import com.example.issuetracker.data.FullIssueDetails
import com.example.issuetracker.utils.IssueParser
import java.io.File
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

class HistoryAdapter(
    private var historyEntries: List<String>,
    private val context: Context,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val issueNumberTextView: TextView = itemView.findViewById(R.id.history_issue_number)
        val shareButton: ImageButton = itemView.findViewById(R.id.history_share_button)
        val deleteButton: ImageButton = itemView.findViewById(R.id.history_delete_button)
        val issueTypeTextView: TextView = itemView.findViewById(R.id.history_issue_type)
        val reasonTextView: TextView = itemView.findViewById(R.id.history_reason)
        val remarksTextView: TextView = itemView.findViewById(R.id.history_remarks)
        val startTimeTextView: TextView = itemView.findViewById(R.id.history_start_time)
        val endTimeTextView: TextView = itemView.findViewById(R.id.history_end_time)
        val durationTextView: TextView = itemView.findViewById(R.id.history_duration)
        val attachmentsGridLayout: GridLayout = itemView.findViewById(R.id.history_attachments_grid)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val entry = historyEntries[position]
        val parsedEntry = IssueParser.parseHistoryEntry(entry)

        val issueNumber = historyEntries.size - position
        holder.issueNumberTextView.text = "Issue #$issueNumber"

        holder.issueTypeTextView.text = "Issue Type: ${parsedEntry["Issue Explanation"] ?: "N/A"}"
        holder.reasonTextView.text = "Reason: ${parsedEntry["Reason"] ?: "N/A"}"
        val remarks = parsedEntry["Issue Remarks"]
        if (remarks != null && remarks.isNotEmpty()) {
            holder.remarksTextView.visibility = View.VISIBLE
            holder.remarksTextView.text = "Remarks: $remarks"
        } else {
            holder.remarksTextView.visibility = View.GONE
        }

        val startTimeIso = parsedEntry["Start Time"]
        val endTimeIso = parsedEntry["End Time"]
        val fillTimeIso = parsedEntry["Fill Time"]

        holder.startTimeTextView.text = "${_formatOnlyDate(startTimeIso ?: "")} at ${_formatTime(startTimeIso ?: "")}"
        holder.endTimeTextView.text = "${_formatOnlyDate(endTimeIso ?: "")} at ${_formatTime(endTimeIso ?: "")}"
        holder.durationTextView.text = "Duration: ${_formatDuration(startTimeIso ?: "", endTimeIso ?: "")}"

        val imagePaths = parsedEntry["Images"]?.split("|")?.filter { it.isNotEmpty() } ?: emptyList()
        if (imagePaths.isNotEmpty()) {
            holder.attachmentsGridLayout.visibility = View.VISIBLE
            displayImages(holder.attachmentsGridLayout, imagePaths)
        } else {
            holder.attachmentsGridLayout.visibility = View.GONE
        }

        holder.shareButton.setOnClickListener { shareIssue(parsedEntry, imagePaths) }
        holder.deleteButton.setOnClickListener { onDeleteClick(position) }

        holder.itemView.setOnClickListener { navigateToDetail(parsedEntry, imagePaths) }
    }

    override fun getItemCount(): Int = historyEntries.size

    fun updateData(newEntries: List<String>) {
        historyEntries = newEntries
        notifyDataSetChanged()
    }

    private fun displayImages(gridLayout: GridLayout, imagePaths: List<String>) {
        gridLayout.removeAllViews()
        gridLayout.columnCount = 3

        val imageSize = context.resources.displayMetrics.widthPixels / 3 - (16 * context.resources.displayMetrics.density).toInt()

        for (imagePath in imagePaths) {
            val imageView = ImageView(context).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = imageSize
                    height = imageSize
                    setMargins(8, 8, 8, 8)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setImageURI(Uri.fromFile(File(imagePath)))
                setOnClickListener { showImageDialog(context, imagePath) }
            }
            gridLayout.addView(imageView)
        }
    }

    private fun showImageDialog(context: Context, imagePath: String) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_image_preview, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.dialog_image_view)
        imageView.setImageURI(Uri.fromFile(File(imagePath)))

        val downloadButton = dialogView.findViewById<ImageView>(R.id.dialog_download_button)
        downloadButton.setOnClickListener {
            val fileUri: Uri? = try {
                FileProvider.getUriForFile(context, "${context.applicationContext.packageName}.provider", File(imagePath))
            } catch (e: IllegalArgumentException) {
                null
            }

            fileUri?.let {
                val shareIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, it)
                    type = context.contentResolver.getType(it)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Image"))
            }
        }

        AlertDialog.Builder(context)
            .setView(dialogView)
            .show()
    }

    private fun shareIssue(parsedEntry: Map<String, String>, imagePaths: List<String>) {
        val message = StringBuilder()
        message.append("*Issue Report*\n\n")
        message.append("*Advisor Name:* ${parsedEntry["Advisor Name"] ?: "N/A"}\n")
        message.append("*CRM ID:* ${parsedEntry["CRM ID"] ?: "N/A"}\n")
        message.append("*Organization:* ${parsedEntry["Organization"] ?: "N/A"}\n\n")
        message.append("*Issue:* ${parsedEntry["Issue Explanation"] ?: "N/A"}\n")
        message.append("*Reason:* ${parsedEntry["Reason"] ?: "N/A"}\n\n")
        message.append("*Start Time:* ${_formatTime(parsedEntry["Start Time"] ?: "")} on ${_formatOnlyDate(parsedEntry["Start Time"] ?: "")}\n")
        message.append("*End Time:* ${_formatTime(parsedEntry["End Time"] ?: "")} on ${_formatOnlyDate(parsedEntry["End Time"] ?: "")}\n")
        message.append("*Duration:* ${_formatDuration(parsedEntry["Start Time"] ?: "", parsedEntry["End Time"] ?: "")}\n")
        message.append("*Fill Time:* ${_formatTime(parsedEntry["Fill Time"] ?: "")} on ${_formatOnlyDate(parsedEntry["Fill Time"] ?: "")}\n")
        parsedEntry["Issue Remarks"]?.let { if (it.isNotEmpty()) message.append("*Remarks:* $it\n") }
        message.append("\nThis report was generated from the Issue Tracker App.")

        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_TEXT, message.toString())

            if (imagePaths.isNotEmpty()) {
                val imageUris: ArrayList<Uri> = ArrayList()
                for (path in imagePaths) {
                    val file = File(path)
                    if (file.exists()) {
                        val fileUri: Uri? = try {
                            FileProvider.getUriForFile(context, "${context.applicationContext.packageName}.provider", file)
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                        fileUri?.let { imageUris.add(it) }
                    }
                }
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Issue Details"))
    }

    private fun navigateToDetail(parsedEntry: Map<String, String>, imagePaths: List<String>) {
        val fullIssueDetails = FullIssueDetails(
            advisorName = parsedEntry["Advisor Name"] ?: "",
            crmId = parsedEntry["CRM ID"] ?: "",
            organization = parsedEntry["Organization"] ?: "",
            issueType = parsedEntry["Issue Explanation"] ?: "",
            reason = parsedEntry["Reason"] ?: "",
            issueRemarks = parsedEntry["Issue Remarks"],
            fillTime = parsedEntry["Fill Time"] ?: "",
            startTime = parsedEntry["Start Time"] ?: "",
            endTime = parsedEntry["End Time"] ?: "",
            imagePaths = imagePaths
        )
        val intent = Intent(context, IssueDetailActivity::class.java)
        intent.putExtra("full_issue_details", fullIssueDetails)
        context.startActivity(intent)
    }

    private fun _formatTime(isoString: String): String {
        return try {
            val dateTime = ZonedDateTime.parse(isoString)
            val hour = dateTime.hour
            val period = if (hour >= 12) "PM" else "AM"
            val formattedHour = if (hour % 12 == 0) 12 else hour % 12
            String.format("%02d:%02d %s", formattedHour, dateTime.minute, period)
        } catch (e: DateTimeParseException) {
            "N/A"
        }
    }

    private fun _formatOnlyDate(isoString: String): String {
        return try {
            val dateTime = ZonedDateTime.parse(isoString)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            dateTime.format(formatter)
        } catch (e: DateTimeParseException) {
            "N/A"
        }
    }

    private fun _formatDuration(startTimeIso: String, endTimeIso: String): String {
        return try {
            val start = ZonedDateTime.parse(startTimeIso)
            val end = ZonedDateTime.parse(endTimeIso)
            val duration = Duration.between(start, end)

            val hours = duration.toHours()
            val minutes = duration.toMinutes() % 60
            val seconds = duration.toSeconds() % 60

            if (hours > 0) {
                "${hours}h ${minutes}m"
            } else if (minutes > 0) {
                "${minutes}m"
            } else {
                "${seconds}s"
            }
        } catch (e: DateTimeParseException) {
            "N/A"
        }
    }
}