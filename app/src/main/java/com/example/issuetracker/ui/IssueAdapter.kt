package com.example.issuetracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.issuetracker.R
import com.example.issuetracker.data.FullIssueDetails
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class IssueAdapter(private val issues: List<FullIssueDetails>, private val onItemClick: (FullIssueDetails) -> Unit) : RecyclerView.Adapter<IssueAdapter.IssueViewHolder>() {

    inner class IssueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.issue_title)
        val descriptionTextView: TextView = itemView.findViewById(R.id.issue_description)
        val statusTextView: TextView = itemView.findViewById(R.id.issue_status)
        val dateTextView: TextView = itemView.findViewById(R.id.issue_date)

        init {
            itemView.setOnClickListener {
                onItemClick(issues[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_issue, parent, false)
        return IssueViewHolder(view)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        val issue = issues[position]
        holder.titleTextView.text = issue.issueType
        holder.descriptionTextView.text = issue.reason
        holder.statusTextView.text = "Recorded" // Placeholder status

        // Format fillTime for display
        try {
            val zonedDateTime = ZonedDateTime.parse(issue.fillTime)
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            holder.dateTextView.text = zonedDateTime.format(formatter)
        } catch (e: Exception) {
            holder.dateTextView.text = issue.fillTime // Fallback to raw string if parsing fails
        }
    }

    override fun getItemCount(): Int {
        return issues.size
    }
}
