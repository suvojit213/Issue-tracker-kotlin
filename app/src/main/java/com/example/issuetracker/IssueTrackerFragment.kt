package com.example.issuetracker

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.issuetracker.databinding.FragmentIssueTrackerBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class IssueTrackerFragment : Fragment() {

    private var _binding: FragmentIssueTrackerBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    private var selectedStartTime: Calendar? = null
    private var selectedEndTime: Calendar? = null
    private var currentPhotoPath: String? = null
    private val attachedImagePaths = mutableListOf<String>()

    private val takePicture = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            currentPhotoPath?.let {
                attachedImagePaths.add(it)
                displayAttachedImages()
            }
        }
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            it.data?.data?.let {
                // Copy the selected image to app-specific storage
                val destinationFile = createImageFile()
                try {
                    requireContext().contentResolver.openInputStream(it)?.use {
                        input -> destinationFile.outputStream().use { output -> input.copyTo(output) }
                    }
                    attachedImagePaths.add(destinationFile.absolutePath)
                    displayAttachedImages()
                } catch (e: IOException) {
                    e.printStackTrace()
                    showSnackbar("Failed to attach image", true)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentIssueTrackerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireActivity().getSharedPreferences("issue_tracker_prefs", Context.MODE_PRIVATE)

        setupSpinners()
        setupTimePickers()
        setupImagePickers()
        setupSubmitButton()
        loadSavedData()
    }

    private fun setupSpinners() {
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.issue_explanation_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.issueExplanationSpinner.adapter = adapter
        }

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.reason_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.reasonSpinner.adapter = adapter
        }
    }

    private fun setupTimePickers() {
        binding.startTimeButton.setOnClickListener { showTimePicker(true) }
        binding.endTimeButton.setOnClickListener { showTimePicker(false) }
    }

    private fun setupImagePickers() {
        binding.attachImageButton.setOnClickListener { showImageSourceDialog() }
    }

    private fun setupSubmitButton() {
        binding.submitButton.setOnClickListener { submitIssue() }
    }

    private fun loadSavedData() {
        binding.advisorNameEditText.setText(sharedPreferences.getString("advisorName", ""))
        binding.crmIdEditText.setText(sharedPreferences.getString("crmId", ""))
        val organization = sharedPreferences.getString("organization", "DISH")
        if (organization == "DISH") {
            binding.radioDish.isChecked = true
        } else {
            binding.radioD2h.isChecked = true
        }
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val c = Calendar.getInstance()
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
                binding.startTimeButton.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(newTime.time)
            } else {
                selectedEndTime = newTime
                binding.endTimeButton.text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(newTime.time)
            }
        }, hour, minute, false)
        tpd.show()
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = android.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Add Image")
        builder.setItems(options) { dialog, item ->
            when (options[item]) {
                "Take Photo" -> dispatchTakePictureIntent()
                "Choose from Gallery" -> dispatchPickImageIntent()
                "Cancel" -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    showSnackbar("Error creating image file", true)
                    null
                }
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.provider",
                        it
                    )
                    currentPhotoPath = it.absolutePath
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    takePicture.launch(takePictureIntent)
                }
            }
        }
    }

    private fun dispatchPickImageIntent() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImage.launch(pickPhotoIntent)
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }

    private fun displayAttachedImages() {
        binding.attachedImagesContainer.removeAllViews()
        attachedImagePaths.forEach { path ->
            val imageView = LayoutInflater.from(requireContext()).inflate(R.layout.item_attached_image, binding.attachedImagesContainer, false) as ImageView
            imageView.setImageURI(Uri.parse(path))
            imageView.setOnClickListener { showImageDialog(Uri.parse(path)) }
            binding.attachedImagesContainer.addView(imageView)
        }
        if (attachedImagePaths.isNotEmpty()) {
            binding.attachedImagesContainer.visibility = View.VISIBLE
        } else {
            binding.attachedImagesContainer.visibility = View.GONE
        }
    }

    private fun showImageDialog(imageUri: Uri) {
        val dialogBuilder = android.app.AlertDialog.Builder(requireContext())
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

    private fun submitIssue() {
        val advisorName = binding.advisorNameEditText.text.toString()
        val crmId = binding.crmIdEditText.text.toString()
        val organization = if (binding.radioDish.isChecked) "DISH" else "D2H"
        val issueExplanation = binding.issueExplanationSpinner.selectedItem.toString()
        val reason = binding.reasonSpinner.selectedItem.toString()
        val issueRemarks = binding.issueRemarksEditText.text.toString()

        if (advisorName.isEmpty() || crmId.isEmpty() || selectedStartTime == null || selectedEndTime == null) {
            showSnackbar("Please fill all required fields and select start/end times.", true)
            return
        }

        // Save data to SharedPreferences
        with(sharedPreferences.edit()) {
            putString("advisorName", advisorName)
            putString("crmId", crmId)
            putString("organization", organization)
            apply()
        }

        val fillTime = Calendar.getInstance()

        val issueDetails = mutableMapOf(
            "Advisor Name" to advisorName,
            "CRM ID" to crmId,
            "Organization" to organization,
            "Issue Explanation" to issueExplanation,
            "Reason" to reason,
            "Start Time" to selectedStartTime!!.timeInMillis.toString(),
            "End Time" to selectedEndTime!!.timeInMillis.toString(),
            "Fill Time" to fillTime.timeInMillis.toString()
        )

        if (issueRemarks.isNotEmpty()) {
            issueDetails["Issue Remarks"] = issueRemarks
        }

        if (attachedImagePaths.isNotEmpty()) {
            issueDetails["Images"] = attachedImagePaths.joinToString("|")
        }

        // Save to history
        val historySet = sharedPreferences.getStringSet("issueHistory", emptySet())?.toMutableSet() ?: mutableSetOf()
        historySet.add(issueDetails.entries.joinToString(", ") { "${it.key}: ${it.value}" })
        sharedPreferences.edit().putStringSet("issueHistory", historySet).apply()

        // Construct Google Form URL
        val googleFormUrl = buildGoogleFormUrl(issueDetails)

        // Open WebView
        binding.webView.apply {
            visibility = View.VISIBLE
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    showSnackbar("Issue submitted successfully!", false)
                    // Optionally clear fields after submission
                    clearFormFields()
                }
            }
            loadUrl(googleFormUrl)
        }
    }

    private fun buildGoogleFormUrl(details: Map<String, String>): String {
        val baseUrl = "https://docs.google.com/forms/d/e/1FAIpQLScx_eJ4--snip--"
        val params = mutableListOf<String>()

        details["Advisor Name"]?.let { params.add("entry.1000000=$it") }
        details["CRM ID"]?.let { params.add("entry.1000001=$it") }
        details["Organization"]?.let { params.add("entry.1000002=$it") }
        details["Issue Explanation"]?.let { params.add("entry.1000003=$it") }
        details["Reason"]?.let { params.add("entry.1000004=$it") }
        details["Issue Remarks"]?.let { params.add("entry.1000005=$it") }

        details["Start Time"]?.let { 
            val cal = Calendar.getInstance().apply { timeInMillis = it.toLong() }
            params.add("entry.1000006_hour=${cal.get(Calendar.HOUR_OF_DAY)}")
            params.add("entry.1000006_minute=${cal.get(Calendar.MINUTE)}")
        }
        details["End Time"]?.let { 
            val cal = Calendar.getInstance().apply { timeInMillis = it.toLong() }
            params.add("entry.1000007_hour=${cal.get(Calendar.HOUR_OF_DAY)}")
            params.add("entry.1000007_minute=${cal.get(Calendar.MINUTE)}")
        }
        details["Fill Time"]?.let { 
            val cal = Calendar.getInstance().apply { timeInMillis = it.toLong() }
            params.add("entry.1000008_year=${cal.get(Calendar.YEAR)}")
            params.add("entry.1000008_month=${cal.get(Calendar.MONTH) + 1}") // Month is 0-indexed
            params.add("entry.1000008_day=${cal.get(Calendar.DAY_OF_MONTH)}")
            params.add("entry.1000009_hour=${cal.get(Calendar.HOUR_OF_DAY)}")
            params.add("entry.1000009_minute=${cal.get(Calendar.MINUTE)}")
        }

        return "$baseUrl?" + params.joinToString("&") { Uri.encodeComponent(it) }
    }

    private fun clearFormFields() {
        binding.issueExplanationSpinner.setSelection(0)
        binding.reasonSpinner.setSelection(0)
        binding.issueRemarksEditText.setText("")
        binding.startTimeButton.text = "Select Start Time"
        binding.endTimeButton.text = "Select End Time"
        selectedStartTime = null
        selectedEndTime = null
        attachedImagePaths.clear()
        displayAttachedImages()
        binding.webView.visibility = View.GONE
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
}

