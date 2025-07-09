package com.example.issuetracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.issuetracker.R
import com.example.issuetracker.data.Issue

class IssueAdapter(private val issues: List<Issue>) : RecyclerView.Adapter<IssueAdapter.IssueViewHolder>() {

    class IssueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.issue_title)
        val descriptionTextView: TextView = itemView.findViewById(R.id.issue_description)
        val statusTextView: TextView = itemView.findViewById(R.id.issue_status)
        val dateTextView: TextView = itemView.findViewById(R.id.issue_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_issue, parent, false)
        return IssueViewHolder(view)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        val issue = issues[position]
        holder.titleTextView.text = issue.title
        holder.descriptionTextView.text = issue.description
        holder.statusTextView.text = issue.status
        holder.dateTextView.text = issue.createdDate
    }

    override fun getItemCount(): Int {
        return issues.size
    }
}
