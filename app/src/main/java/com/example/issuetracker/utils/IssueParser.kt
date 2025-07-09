package com.example.issuetracker.utils

object IssueParser {
    fun parseHistoryEntry(entry: String): Map<String, String> {
        val parsedMap = mutableMapOf<String, String>()
        val parts = entry.split("|")
        for (part in parts) {
            val keyValue = part.split(":", 2) // Split only on the first colon
            if (keyValue.size == 2) {
                parsedMap[keyValue[0].trim()] = keyValue[1].trim()
            }
        }
        return parsedMap
    }
}