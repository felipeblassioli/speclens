package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "review_sessions")
data class ReviewSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val fileName: String,
    val status: String, // "Triaging", "In Review", "Changed Since Review", "Draft"
    val snapshot: String,
    val lastReviewedText: String, // e.g. "Today", "Yesterday"
    val changedSinceReview: Boolean,
    val markdownContent: String
)

@Entity(tableName = "private_notes")
data class PrivateNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val sectionName: String, // e.g. "Data Flow"
    val filePath: String, // e.g. "src/middleware/auth.js"
    val lineNumber: Int, // e.g. 6
    val noteType: String, // "Concern", "Question", "Trade-off", "Clarity"
    val severityGuess: String, // "Blocking", "Major", "Minor", "Nit", "None"
    val content: String,
    val isPromoted: Boolean = false,
    val isDiscarded: Boolean = false
)

@Entity(tableName = "findings")
data class Finding(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: Int,
    val sourceNoteId: Int? = null,
    val title: String,
    val linkedSection: String, // e.g. "Data Flow"
    val severity: String, // "Blocking", "Major", "Minor", "Nit"
    val evidenceInternal: String,
    val draftWording: String,
    val isAccepted: Boolean = false, // true = Accepted, false = Candidate
    val isDiscarded: Boolean = false
)
