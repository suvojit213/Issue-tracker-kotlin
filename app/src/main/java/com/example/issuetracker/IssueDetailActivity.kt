package com.example.issuetracker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.issuetracker.databinding.ActivityIssueDetailBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class IssueDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIssueDetailBinding
    private lateinit var issueDetails: Map<String, String>
    private lateinit var imagePaths: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIssueDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Issue Details"

        issueDetails = intent.getStringArrayExtra("issue_details")?.associate { it.split(": ", limit = 2)[0] to it.split(": ", limit = 2)[1] } ?: emptyMap()
        imagePaths = issueDetails["Images"]?.split("|") ?: emptyList()

        displayIssueDetails()
        setupShareButton()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun displayIssueDetails() {
        // Issue Information
        binding.advisorNameValue.text = issueDetails["Advisor Name"] ?: "N/A"
        binding.crmIdValue.text = issueDetails["CRM ID"] ?: "N/A"
        binding.organizationValue.text = issueDetails["Organization"] ?: "N/A"
        binding.issueTypeValue.text = issueDetails["Issue Explanation"] ?: "N/A"
        binding.reasonValue.text = issueDetails["Reason"] ?: "N/A"
        val remarks = issueDetails["Issue Remarks"]
        if (!remarks.isNullOrEmpty()) {
            binding.remarksLayout.visibility = View.VISIBLE
            binding.remarksValue.text = remarks
        } else {
            binding.remarksLayout.visibility = View.GONE
        }

        // Time Information
        binding.fillTimeValue.text = "${formatOnlyDate(issueDetails["Fill Time"] ?: "")}" + " at " + "${formatTime(issueDetails["Fill Time"] ?: "")}"
        binding.startTimeValue.text = "${formatOnlyDate(issueDetails["Start Time"] ?: "")}" + " at " + "${formatTime(issueDetails["Start Time"] ?: "")}"
        binding.endTimeValue.text = "${formatOnlyDate(issueDetails["End Time"] ?: "")}" + " at " + "${formatTime(issueDetails["End Time"] ?: "")}"
        binding.durationValue.text = formatDuration(issueDetails["Start Time"] ?: "", issueDetails["End Time"] ?: "")

        // Attachments
        if (imagePaths.isNotEmpty()) {
            binding.attachmentsSection.visibility = View.VISIBLE
            imagePaths.forEach { path ->
                val imageView = LayoutInflater.from(this).inflate(R.layout.item_image_detail, binding.imageGrid, false) as ImageView
                val imageFile = File(path)
                if (imageFile.exists()) {
                    imageView.setImageURI(Uri.fromFile(imageFile))
                    imageView.setOnClickListener { showImageDialog(FileProvider.getUriForFile(this, "${applicationContext.packageName}.provider", imageFile)) }
                }
                binding.imageGrid.addView(imageView)
            }
        } else {
            binding.attachmentsSection.visibility = View.GONE
        }
    }

    private fun setupShareButton() {
        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_share -> {
                    shareIssue()
                    true
                }
                else -> false
            }
        }
    }

    private fun formatTime(timeInMillis: String): String {
        return try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.format(Date(timeInMillis.toLong()))
        } catch (e: Exception) {
            "N/A"
        }
    }

    private fun formatOnlyDate(timeInMillis: String): String {
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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

    private fun shareIssue() {
        val message = buildShareMessage(issueDetails)
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

    private fun showImageDialog(imageUri: Uri) {
        val dialogBuilder = android.app.AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_image_viewer, null)
        dialogBuilder.setView(dialogView)

        val imageView = dialogView.findViewById<ImageView>(R.id.dialog_image_view)
        imageView.setImageURI(imageUri)

        val downloadButton = dialogView.findViewById<ImageView>(R.id.download_button)
        downloadButton.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "image/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(Intent.createChooser(shareIntent, "Share Image"))
        }

        val alertDialog = dialogBuilder.create()
        alertDialog.show()
    }
}

