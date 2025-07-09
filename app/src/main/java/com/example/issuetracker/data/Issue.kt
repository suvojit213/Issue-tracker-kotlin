package com.example.issuetracker.data

data class Issue(
    val id: String,
    val title: String,
    val description: String,
    val status: String,
    val createdDate: String
)
