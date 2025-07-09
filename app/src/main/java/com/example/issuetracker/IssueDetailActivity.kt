package com.example.issuetracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView
import com.example.issuetracker.data.FullIssueDetails
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import android.view.View
import android.widget.GridLayout
import android.widget.ImageView
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater

class IssueDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_issue_detail)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Enable back button
        supportActionBar?.title = "Issue Details"

        val issueDetails = intent.getParcelableExtra<FullIssueDetails>("full_issue_details")

        issueDetails?.let {
            // Issue Information
            findViewById<TextView>(R.id.detail_advisor_name).text = "Advisor Name: ${it.advisorName}"
            findViewById<TextView>(R.id.detail_crm_id).text = "CRM ID: ${it.crmId}"
            findViewById<TextView>(R.id.detail_organization).text = "Organization: ${it.organization}"
            findViewById<TextView>(R.id.detail_issue_type).text = "Issue Type: ${it.issueType}"
            findViewById<TextView>(R.id.detail_reason).text = "Reason: ${it.reason}"
            findViewById<TextView>(R.id.detail_remarks).text = "Remarks: ${it.issueRemarks ?: "N/A"}"

            // Time Information
            findViewById<TextView>(R.id.detail_fill_time).text = "Fill Time: ${_formatOnlyDate(it.fillTime)} at ${_formatTime(it.fillTime)}"
            findViewById<TextView>(R.id.detail_start_time).text = "Start Time: ${_formatOnlyDate(it.startTime)} at ${_formatTime(it.startTime)}"
            findViewById<TextView>(R.id.detail_end_time).text = "End Time: ${_formatOnlyDate(it.endTime)} at ${_formatTime(it.endTime)}"
            findViewById<TextView>(R.id.detail_duration).text = "Duration: ${_formatDuration(it.startTime, it.endTime)}"

            // Attachments
            val attachmentsCard = findViewById<androidx.cardview.widget.CardView>(R.id.attachments_card)
            val attachmentsGrid = findViewById<GridLayout>(R.id.attachments_grid)

            if (it.imagePaths.isEmpty()) {
                attachmentsCard.visibility = View.GONE
            } else {
                attachmentsCard.visibility = View.VISIBLE
                displayImages(attachmentsGrid, it.imagePaths)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.detail_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.action_share -> {
                val issueDetails = intent.getParcelableExtra<FullIssueDetails>("full_issue_details")
                issueDetails?.let { shareIssue(it) }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun displayImages(gridLayout: GridLayout, imagePaths: List<String>) {
        gridLayout.removeAllViews()
        gridLayout.columnCount = 3 // As per Flutter's GridView.builder

        val imageSize = resources.displayMetrics.widthPixels / 3 - (16 * resources.displayMetrics.density).toInt() // Roughly 1/3 of screen width minus padding

        for (imagePath in imagePaths) {
            val imageView = ImageView(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = imageSize
                    height = imageSize
                    setMargins(8, 8, 8, 8)
                }
                scaleType = ImageView.ScaleType.CENTER_CROP
                // Load image from file path
                setImageURI(Uri.fromFile(File(imagePath)))
                setOnClickListener { showImageDialog(imagePath) }
            }
            gridLayout.addView(imageView)
        }
    }

    private fun showImageDialog(imagePath: String) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_image_preview, null)
        val imageView = dialogView.findViewById<ImageView>(R.id.dialog_image_view)
        imageView.setImageURI(Uri.fromFile(File(imagePath)))

        val downloadButton = dialogView.findViewById<ImageView>(R.id.dialog_download_button)
        downloadButton.setOnClickListener {
            // Share the image file
            val fileUri: Uri? = try {
                FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", File(imagePath))
            } catch (e: IllegalArgumentException) {
                null
            }

            fileUri?.let {
                val shareIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, it)
                    type = contentResolver.getType(it)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Share Image"))
            }
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .show()
    }

    private fun shareIssue(issueDetails: FullIssueDetails) {
        val message = StringBuilder()
        message.append("*Issue Report*\n\n")
        message.append("*Advisor Name:* ${issueDetails.advisorName}\n")
        message.append("*CRM ID:* ${issueDetails.crmId}\n")
        message.append("*Organization:* ${issueDetails.organization}\n\n")
        message.append("*Issue:* ${issueDetails.issueType}\n")
        message.append("*Reason:* ${issueDetails.reason}\n\n")
        message.append("*Start Time:* ${_formatTime(issueDetails.startTime)} on ${_formatOnlyDate(issueDetails.startTime)}\n")
        message.append("*End Time:* ${_formatTime(issueDetails.endTime)} on ${_formatOnlyDate(issueDetails.endTime)}\n")
        message.append("*Duration:* ${_formatDuration(issueDetails.startTime, issueDetails.endTime)}\n")
        message.append("*Fill Time:* ${_formatTime(issueDetails.fillTime)} on ${_formatOnlyDate(issueDetails.fillTime)}\n")
        issueDetails.issueRemarks?.let { if (it.isNotEmpty()) message.append("*Remarks:* $it\n") }
        message.append("\nThis report was generated from the Issue Tracker App.")

        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_TEXT, message.toString())

            if (issueDetails.imagePaths.isNotEmpty()) {
                val imageUris: ArrayList<Uri> = ArrayList()
                for (path in issueDetails.imagePaths) {
                    val file = File(path)
                    if (file.exists()) {
                        val fileUri: Uri? = try {
                            FileProvider.getUriForFile(this@IssueDetailActivity, "${applicationContext.packageName}.provider", file)
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
        startActivity(Intent.createChooser(shareIntent, "Share Issue Details"))
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
