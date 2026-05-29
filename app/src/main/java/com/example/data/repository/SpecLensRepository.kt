package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SpecLensRepository(
    private val sessionDao: ReviewSessionDao,
    private val noteDao: PrivateNoteDao,
    private val findingDao: FindingDao
) {
    val allSessions: Flow<List<ReviewSession>> = sessionDao.getAllSessions()

    fun getSessionById(id: Int): Flow<ReviewSession?> = sessionDao.getSessionById(id)

    fun getNotesForSession(sessionId: Int): Flow<List<PrivateNote>> = noteDao.getNotesForSession(sessionId)

    fun getFindingsForSession(sessionId: Int): Flow<List<Finding>> = findingDao.getFindingsForSession(sessionId)

    suspend fun insertSession(session: ReviewSession): Long = sessionDao.insertSession(session)

    suspend fun updateSession(session: ReviewSession) = sessionDao.updateSession(session)

    suspend fun deleteSession(session: ReviewSession) = sessionDao.deleteSession(session)

    suspend fun insertNote(note: PrivateNote): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: PrivateNote) = noteDao.updateNote(note)

    suspend fun deleteNote(note: PrivateNote) = noteDao.deleteNote(note)

    suspend fun insertFinding(finding: Finding): Long = findingDao.insertFinding(finding)

    suspend fun updateFinding(finding: Finding) = findingDao.updateFinding(finding)

    suspend fun deleteFinding(finding: Finding) = findingDao.deleteFinding(finding)

    suspend fun getNoteById(id: Int): PrivateNote? = noteDao.getNoteById(id)

    suspend fun getFindingById(id: Int): Finding? = findingDao.getFindingById(id)

    suspend fun prepopulateIfEmpty() {
        val currentSessions = allSessions.first()
        if (currentSessions.isNotEmpty()) return

        // 1. Authentication Flow Migration
        val session1Id = sessionDao.insertSession(
            ReviewSession(
                title = "Authentication Flow Migration",
                fileName = "auth-service-v2.md",
                status = "Triaging",
                snapshot = "8a2f1b",
                lastReviewedText = "Today",
                changedSinceReview = false,
                markdownContent = """
# Authentication Flow Migration

## 2. Goals
Migrate the existing v1 token validation middleware to the new centralized authentication service. This will ensure all incoming requests are routed through a single identity provider boundary before hitting the domain logic.

* Reduce token validation latency by 15% using the new gRPC endpoints.
* Deprecate the legacy user-sessions table.

## Section 03 Data Flow
The primary entry point for all API requests will now pass through the API Gateway, which intercepts the Authorization header and proxies it.

`src/middleware/auth.js` code contains strict token validation middleware.

## 2.1 Security Constraints
All service-to-service communication must use mTLS. The legacy shared-secret approach is explicitly forbidden in the v2 architecture.

## 4. Rollout Plan
We will execute a shadow rollout starting at 5% traffic, monitoring the 4xx/5xx error rates closely before ramping up.
                """.trimIndent()
            )
        ).toInt()

        // Populating Private Notes for Session 1 (Needs 3 raw notes in screenshot stats)
        // One is raw note shown on screen
        noteDao.insertNote(
            PrivateNote(
                sessionId = session1Id,
                sectionName = "Data Flow",
                filePath = "src/middleware/auth.js",
                lineNumber = 6,
                noteType = "Concern",
                severityGuess = "Blocking",
                content = "The token validation path still has a TODO. We should not proceed until strict validation behavior is defined.",
                isPromoted = false,
                isDiscarded = false
            )
        )
        // Two other notes
        noteDao.insertNote(
            PrivateNote(
                sessionId = session1Id,
                sectionName = "Goals",
                filePath = "auth-service-v2.md",
                lineNumber = 5,
                noteType = "Question",
                severityGuess = "Minor",
                content = "How does this 15% latency reduction compare to our current regional gateway overhead?",
                isPromoted = false,
                isDiscarded = false
            )
        )
        noteDao.insertNote(
            PrivateNote(
                sessionId = session1Id,
                sectionName = "Rollout Plan",
                filePath = "auth-service-v2.md",
                lineNumber = 24,
                noteType = "Trade-off",
                severityGuess = "Minor",
                content = "A shadow rollout at 5% is safe, but we will need synthetic test traffic to verify latency targets since natural traffic is bursty.",
                isPromoted = false,
                isDiscarded = false
            )
        )

        // Findings for Session 1: 1 Candidate, 1 Accepted
        findingDao.insertFinding(
            Finding(
                sessionId = session1Id,
                title = "Missing MFA fallback logic",
                linkedSection = "Problem",
                severity = "Blocking",
                evidenceInternal = "The spec describes the primary TOTP flow but does not define recovery behavior.",
                draftWording = "The design describes the primary TOTP flow but does not define what happens when a user loses access to their device. Please specify the recovery path, including recovery codes, support escalation, or alternative verification.",
                isAccepted = false, // Candidate
                isDiscarded = false
            )
        )
        findingDao.insertFinding(
            Finding(
                sessionId = session1Id,
                title = "Rate limits are not specified",
                linkedSection = "Goals",
                severity = "Major",
                evidenceInternal = "The login endpoint needs exact burst and sustained rate limits to reduce brute-force risk.",
                draftWording = "The login endpoint needs exact burst and sustained rate limits to reduce brute-force risk. Ambiguous definitions are unacceptable for authentication routing.",
                isAccepted = true, // Accepted
                isDiscarded = false
            )
        )


        // 2. Payment Gateway Routing
        val session2Id = sessionDao.insertSession(
            ReviewSession(
                title = "Payment Gateway Routing",
                fileName = "payment-gateway-routing.md",
                status = "In Review",
                snapshot = "3f9d2c",
                lastReviewedText = "Yesterday",
                changedSinceReview = true,
                markdownContent = """
# Payment Gateway Routing Design

## 1. Context & Background
Currently we process all payment transactions using a single backup provider. This spec introduces multi-gateway intelligent routing to minimize processor fee structure and improve multi-region availability.

## 2. Dynamic Routing Logic
Transactions are dynamically evaluated at runtime against processor health metrics and currency tables.

`routes/payments.kt` controls transactions.

## 3. Fallback Mechanisms
When a payment processor failure threshold is crossed (e.g. 3 consecutive timeouts), the route engine switches seamlessly to a predefined secondary gateway with automatic retry logic.
                """.trimIndent()
            )
        ).toInt()

        // Session 2 Stats: Private notes: 5, Candidate findings: 2, Accepted findings: 0
        for (i in 1..5) {
            noteDao.insertNote(
                PrivateNote(
                    sessionId = session2Id,
                    sectionName = "Dynamic Routing Logic",
                    filePath = "routes/payments.kt",
                    lineNumber = 10 + i,
                    noteType = if (i % 2 == 0) "Question" else "Clarity",
                    severityGuess = "Minor",
                    content = "Private note #$i regarding payment latency dynamic checks in regional gateway routing paths.",
                    isPromoted = false,
                    isDiscarded = false
                )
            )
        }
        findingDao.insertFinding(
            Finding(
                sessionId = session2Id,
                title = "Consolidated processor connection pool limits",
                linkedSection = "Dynamic Routing Logic",
                severity = "Major",
                evidenceInternal = "Connection boundaries are undefined during processor swap bursts.",
                draftWording = "We should specify maximum thread pool metrics for payment adapters during switches to prevent deadlocks.",
                isAccepted = false,
                isDiscarded = false
            )
        )
        findingDao.insertFinding(
            Finding(
                sessionId = session2Id,
                title = "Fallback retries could exacerbate timeout storm",
                linkedSection = "Fallback Mechanisms",
                severity = "Major",
                evidenceInternal = "Retries with plain exponential delays lack random jitter.",
                draftWording = "Please specify full random jitter factor in retry queues to prevent systemic load surges.",
                isAccepted = false,
                isDiscarded = false
            )
        )


        // 3. Ledger Reconciliation Design
        val session3Id = sessionDao.insertSession(
            ReviewSession(
                title = "Ledger Reconciliation Design",
                fileName = "ledger-reconciliation-design.md",
                status = "Changed Since Review",
                snapshot = "1b4e9f",
                lastReviewedText = "2 days ago",
                changedSinceReview = true,
                markdownContent = """
# Ledger Reconciliation Architecture

## 1. Goals & System Design
The ledger reconciliation process validates transactions across core database tables and third-party bank statements to guarantee 100% financial integrity.

## 2. High-Throughput Reconciliation
To prevent blocking production locks, statement parsing runs as an offline asynchronous task scheduled at low-traffic hours.

`jobs/reconciliation.go` processes batch statements.

## 3. Disaster Recovery Scenario
If a mismatch is detected, automated alerts are raised, the mismatch is written to an anomaly registry, and secondary audit queues are triggered.
                """.trimIndent()
            )
        ).toInt()

        // Session 3 Stats: Private notes: 8, Candidate findings: 2, Accepted findings: 2
        for (i in 1..8) {
            noteDao.insertNote(
                PrivateNote(
                    sessionId = session3Id,
                    sectionName = "High-Throughput Reconciliation",
                    filePath = "jobs/reconciliation.go",
                    lineNumber = 90 + i,
                    noteType = "Trade-off",
                    severityGuess = "Nit",
                    content = "Raw ledger matching note details #$i concerning secondary tables index optimizations.",
                    isPromoted = false,
                    isDiscarded = false
                )
            )
        }
        findingDao.insertFinding(
            Finding(
                sessionId = session3Id,
                title = "Anomalies lack historical traceability",
                linkedSection = "Disaster Recovery Scenario",
                severity = "Minor",
                evidenceInternal = "Mismatch events omit exact record sequence snapshots.",
                draftWording = "Anomaly audits must log sequence IDs as mono-time markers to solve statements trace sync issues.",
                isAccepted = false,
                isDiscarded = false
            )
        )
        findingDao.insertFinding(
            Finding(
                sessionId = session3Id,
                title = "Concurrency locking in batch match lists",
                linkedSection = "High-Throughput Reconciliation",
                severity = "Blocking",
                evidenceInternal = "Table reconciliation uses broad database transaction locks.",
                draftWording = "Reconciliation loops should stream page-scoped entries using optimistic locking instead of raw database locks.",
                isAccepted = false,
                isDiscarded = false
            )
        )
        findingDao.insertFinding(
            Finding(
                sessionId = session3Id,
                title = "Missing disk safety metrics on match logs",
                linkedSection = "Goals & System Design",
                severity = "Minor",
                evidenceInternal = "Local statement buffers aren't written to write-ahead disks.",
                draftWording = "Mismatch triggers need sync-to-disk guarantees before signaling alerting queues.",
                isAccepted = true,
                isDiscarded = false
            )
        )
        findingDao.insertFinding(
            Finding(
                sessionId = session3Id,
                title = "Audit task has no run deadline",
                linkedSection = "High-Throughput Reconciliation",
                severity = "Major",
                evidenceInternal = "Reconciliation schedules run for up to 3 hours without timeouts.",
                draftWording = "Enforce a strict 15-minute execution limit on daily transaction sync run tasks.",
                isAccepted = true,
                isDiscarded = false
            )
        )
    }
}
