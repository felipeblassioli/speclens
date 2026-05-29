package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.database.Finding
import com.example.data.database.PrivateNote
import com.example.data.database.ReviewSession
import com.example.data.repository.SpecLensRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class NavigationTab {
    SESSIONS,
    REVIEW,
    TRIAGE,
    PUBLISH
}

class SpecLensViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SpecLensRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = SpecLensRepository(
            database.reviewSessionDao(),
            database.privateNoteDao(),
            database.findingDao()
        )
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    // Tab Flow
    private val _currentTab = MutableStateFlow(NavigationTab.SESSIONS)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    // Sessions Flow
    val allSessions: StateFlow<List<ReviewSession>> = repository.allSessions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current Session Flow
    private val _selectedSessionId = MutableStateFlow<Int?>(1) // Default to first session
    val selectedSessionId: StateFlow<Int?> = _selectedSessionId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentSession: StateFlow<ReviewSession?> = _selectedSessionId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getSessionById(id)
            } else {
                flowOf(null)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Current Session's Private Notes Flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val notesForCurrentSession: StateFlow<List<PrivateNote>> = _selectedSessionId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getNotesForSession(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Current Session's Structured Findings Flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val findingsForCurrentSession: StateFlow<List<Finding>> = _selectedSessionId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getFindingsForSession(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Sheets State
    private val _showAddNoteSheet = MutableStateFlow(false)
    val showAddNoteSheet: StateFlow<Boolean> = _showAddNoteSheet.asStateFlow()

    // Note linked metadata for the drawer
    private val _addNoteLineContext = MutableStateFlow<Pair<String, Int>?>(null) // Pair(filePath, lineNumber)
    val addNoteLineContext: StateFlow<Pair<String, Int>?> = _addNoteLineContext.asStateFlow()

    private val _showTriageNoteSheet = MutableStateFlow<PrivateNote?>(null)
    val showTriageNoteSheet: StateFlow<PrivateNote?> = _showTriageNoteSheet.asStateFlow()

    private val _showFindingEditSheet = MutableStateFlow<Finding?>(null)
    val showFindingEditSheet: StateFlow<Finding?> = _showFindingEditSheet.asStateFlow()

    // Publish Custom Settings
    private val _markdownIncludeSummaryStats = MutableStateFlow(false)
    val markdownIncludeSummaryStats: StateFlow<Boolean> = _markdownIncludeSummaryStats.asStateFlow()

    fun setTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun selectSession(sessionId: Int) {
        _selectedSessionId.value = sessionId
        _currentTab.value = NavigationTab.REVIEW
    }

    fun selectSessionWithoutTabChange(sessionId: Int) {
        _selectedSessionId.value = sessionId
    }

    fun openAddNoteSheet(filePath: String, lineNumber: Int) {
        _addNoteLineContext.value = Pair(filePath, lineNumber)
        _showAddNoteSheet.value = true
    }

    fun closeAddNoteSheet() {
        _showAddNoteSheet.value = false
    }

    fun openTriageNoteSheet(note: PrivateNote) {
        _showTriageNoteSheet.value = note
    }

    fun closeTriageNoteSheet() {
        _showTriageNoteSheet.value = null
    }

    fun openFindingEditSheet(finding: Finding) {
        _showFindingEditSheet.value = finding
    }

    fun closeFindingEditSheet() {
        _showFindingEditSheet.value = null
    }

    fun setIncludeSummaryStats(include: Boolean) {
        _markdownIncludeSummaryStats.value = include
    }

    // Actions
    fun createSessionFromSample(title: String, fileName: String, content: String) {
        viewModelScope.launch {
            val shortHash = (100000..999999).random().toString(16)
            val newId = repository.insertSession(
                ReviewSession(
                    title = title,
                    fileName = fileName,
                    status = "In Review",
                    snapshot = shortHash,
                    lastReviewedText = "Today",
                    changedSinceReview = false,
                    markdownContent = content
                )
            ).toInt()
            selectSession(newId)
        }
    }

    fun createCustomSession(title: String, fileName: String) {
        viewModelScope.launch {
            val shortHash = (100000..999999).random().toString(16)
            val headerTitle = if (title.isBlank()) "Untitled Specification" else title
            val file = if (fileName.isBlank()) "design-doc.md" else if (fileName.endsWith(".md")) fileName else "$fileName.md"
            val newId = repository.insertSession(
                ReviewSession(
                    title = headerTitle,
                    fileName = file,
                    status = "In Review",
                    snapshot = shortHash,
                    lastReviewedText = "Today",
                    changedSinceReview = false,
                    markdownContent = """
# $headerTitle

## 1. Overview
Describe the high-level architecture and scope here.

## 2. System Architecture
Outline the main services, databases, and APIs.

`src/index.js` contains the main routing matrix.

## 3. Deployment & Rollout Plan
Explain how this transitions safely to production.
                    """.trimIndent()
                )
            ).toInt()
            selectSession(newId)
        }
    }

    fun addPrivateNote(
        sectionName: String,
        filePath: String,
        lineNumber: Int,
        noteType: String,
        severityGuess: String,
        content: String
    ) {
        val currentId = _selectedSessionId.value ?: return
        viewModelScope.launch {
            repository.insertNote(
                PrivateNote(
                    sessionId = currentId,
                    sectionName = sectionName,
                    filePath = filePath,
                    lineNumber = lineNumber,
                    noteType = noteType,
                    severityGuess = severityGuess,
                    content = content
                )
            )
        }
    }

    fun discardNote(noteId: Int) {
        viewModelScope.launch {
            val note = repository.getNoteById(noteId)
            if (note != null) {
                repository.updateNote(note.copy(isDiscarded = true))
            }
        }
    }

    fun promoteNoteToFinding(
        noteId: Int,
        title: String,
        severity: String,
        linkedSection: String,
        evidenceInternal: String,
        draftWording: String,
        isAccepted: Boolean
    ) {
        val currentId = _selectedSessionId.value ?: return
        viewModelScope.launch {
            val note = repository.getNoteById(noteId)
            if (note != null) {
                repository.updateNote(note.copy(isPromoted = true))
                repository.insertFinding(
                    Finding(
                        sessionId = currentId,
                        sourceNoteId = noteId,
                        title = title,
                        linkedSection = linkedSection,
                        severity = severity,
                        evidenceInternal = evidenceInternal,
                        draftWording = draftWording,
                        isAccepted = isAccepted
                    )
                )
            }
            closeTriageNoteSheet()
        }
    }

    fun updateFinding(
        findingId: Int,
        title: String,
        linkedSection: String,
        severity: String,
        evidenceInternal: String,
        draftWording: String,
        isAccepted: Boolean,
        isDiscarded: Boolean
    ) {
        viewModelScope.launch {
            val finding = repository.getFindingById(findingId)
            if (finding != null) {
                repository.updateFinding(
                    finding.copy(
                        title = title,
                        linkedSection = linkedSection,
                        severity = severity,
                        evidenceInternal = evidenceInternal,
                        draftWording = draftWording,
                        isAccepted = isAccepted,
                        isDiscarded = isDiscarded
                    )
                )
            }
            closeFindingEditSheet()
        }
    }

    fun promoteFindingDirect(findingId: Int) {
        viewModelScope.launch {
            val finding = repository.getFindingById(findingId)
            if (finding != null) {
                repository.updateFinding(finding.copy(isAccepted = true))
            }
        }
    }

    fun demoteFindingDirect(findingId: Int) {
        viewModelScope.launch {
            val finding = repository.getFindingById(findingId)
            if (finding != null) {
                repository.updateFinding(finding.copy(isAccepted = false))
            }
        }
    }

    fun discardFindingDirect(findingId: Int) {
        viewModelScope.launch {
            val finding = repository.getFindingById(findingId)
            if (finding != null) {
                repository.updateFinding(finding.copy(isDiscarded = true))
            }
        }
    }

    fun distillNotes() {
        val currentId = _selectedSessionId.value ?: return
        val currentNotes = notesForCurrentSession.value
        val openNotes = currentNotes.filter { !it.isPromoted && !it.isDiscarded }
        if (openNotes.isEmpty()) return

        viewModelScope.launch {
            // Distill opens into candidate findings dynamically
            openNotes.forEach { note ->
                repository.updateNote(note.copy(isPromoted = true))
                repository.insertFinding(
                    Finding(
                        sessionId = currentId,
                        sourceNoteId = note.id,
                        title = "Distilled: " + (if (note.content.length > 30) note.content.take(30) + "..." else note.content),
                        linkedSection = note.sectionName,
                        severity = note.severityGuess,
                        evidenceInternal = note.content,
                        draftWording = "Under review section ${note.sectionName} at ${note.filePath} line ${note.lineNumber}:\n\n${note.content}",
                        isAccepted = false, // starts as candidate
                        isDiscarded = false
                    )
                )
            }
        }
    }
}
