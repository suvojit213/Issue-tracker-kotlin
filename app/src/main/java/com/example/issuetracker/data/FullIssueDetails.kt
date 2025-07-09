package com.example.issuetracker.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class FullIssueDetails(
    val advisorName: String,
    val crmId: String,
    val organization: String,
    val issueType: String, // Corresponds to 'Issue Explanation'
    val reason: String,
    val issueRemarks: String?,
    val fillTime: String,
    val startTime: String,
    val endTime: String,
    val imagePaths: List<String>
) : Parcelable