package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.database.ReviewSession
import com.example.ui.theme.*
import com.example.ui.viewmodel.SpecLensViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionsScreen(
    viewModel: SpecLensViewModel,
    sessions: List<ReviewSession>,
    onStartCustomSession: (String, String) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newFile by remember { mutableStateOf("") }

    if (showCreateDialog) {
        Dialog(onDismissRequest = { showCreateDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium),
                color = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "New Review Session",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "SPECIFICATION TITLE",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = newTitle,
                            onValueChange = { newTitle = it },
                            placeholder = { Text("e.g. Identity Management") },
                            modifier = Modifier.fillMaxWidth().testTag("custom_session_title_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "FILE NAME",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = newFile,
                            onValueChange = { newFile = it },
                            placeholder = { Text("e.g. identity-v2.md") },
                            modifier = Modifier.fillMaxWidth().testTag("custom_session_file_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showCreateDialog = false },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                onStartCustomSession(newTitle, newFile)
                                newTitle = ""
                                newFile = ""
                                showCreateDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.minimumInteractiveComponentSize().testTag("confirm_create_session"),
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text("Create")
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "SpecLens Console Icon",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "SPECLENS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            ),
                            letterSpacing = 2.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { /* Reload or check triggers automatically */ },
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Synchronize reviews icon",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 80.dp) // Offset clear above bottom bar space
                    .minimumInteractiveComponentSize()
                    .testTag("start_review_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add review")
                    Text(
                        text = "Start Review",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Active Sessions",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (sessions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No active review sessions.\nUse the templates below or the FAB to start.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp,
                            modifier = Modifier.testTag("empty_sessions_warning")
                        )
                    }
                }
            } else {
                items(sessions) { session ->
                    SessionCard(
                        session = session,
                        onClick = { viewModel.selectSession(session.id) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Start from Sample Spec",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Sample Spec List
            item {
                SampleTemplateItem(
                    title = "Authentication Flow Migration",
                    fileName = "auth-service-v2.md",
                    onClick = {
                        viewModel.createSessionFromSample(
                            "Authentication Flow Migration",
                            "auth-service-v2.md",
                            """
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
                    }
                )
            }

            item {
                SampleTemplateItem(
                    title = "Payment Gateway Routing",
                    fileName = "payment-gateway-routing.md",
                    onClick = {
                        viewModel.createSessionFromSample(
                            "Payment Gateway Routing",
                            "payment-gateway-routing.md",
                            """
# Payment Gateway Routing Design

## 1. Context & Background
Currently we process all payment transactions using a single backup provider. This spec introduces multi-gateway intelligent routing to minimize processor fee structure and improve multi-region availability.

## 2. Dynamic Routing Logic
Transactions are dynamically evaluated at runtime against processor health metrics and currency tables.

`routes/payments.kt` controls transactions routing matrix boundaries.

## 3. Fallback Mechanisms
When a payment processor failure threshold is crossed (e.g. 3 consecutive timeouts), the route engine switches seamlessly to a predefined secondary gateway with automatic retry logic.
                            """.trimIndent()
                        )
                    }
                )
            }

            item {
                SampleTemplateItem(
                    title = "Ledger Reconciliation Design",
                    fileName = "ledger-reconciliation-design.md",
                    onClick = {
                        viewModel.createSessionFromSample(
                            "Ledger Reconciliation Design",
                            "ledger-reconciliation-design.md",
                            """
# Ledger Reconciliation Architecture

## 1. Goals & System Design
The ledger reconciliation process validates transactions across core database tables and third-party bank statements to guarantee 100% financial integrity.

## 2. High-Throughput Reconciliation
To prevent blocking production locks, statement parsing runs as an offline asynchronous task scheduled at low-traffic hours.

`jobs/reconciliation.go` handles mismatch matches.

## 3. Disaster Recovery Scenario
If a mismatch is detected, automated alerts are raised, the mismatch is written to an anomaly registry, and secondary audit queues are triggered.
                            """.trimIndent()
                        )
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
fun SessionCard(session: ReviewSession, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("session_card_${session.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // MD File Badge Frame
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Spec Icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    // Visual tag "MD"
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 1.dp, y = 2.dp)
                            .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                            .padding(horizontal = 2.dp, vertical = 0.5.dp)
                    ) {
                        Text(
                            text = "MD",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 7.sp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = session.fileName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Chips: Status tag & Snapshot ID in Mono style
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Status Color Chip
                val containerColor = when (session.status) {
                    "Triaging" -> MaterialTheme.colorScheme.tertiaryContainer
                    "In Review" -> MaterialTheme.colorScheme.primaryContainer
                    "Changed Since Review" -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
                val contentColor = when (session.status) {
                    "Triaging" -> MaterialTheme.colorScheme.onTertiaryContainer
                    "In Review" -> MaterialTheme.colorScheme.onPrimaryContainer
                    "Changed Since Review" -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(containerColor)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = session.status,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                        color = contentColor
                    )
                }

                // Snapshot MD
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                ) {
                    Text(
                        text = "Snapshot ${session.snapshot}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stats Label Text
            // For custom mock labels matching screenshot states, we will display session statistics
            val notesCount = when (session.id) {
                1 -> 3
                2 -> 5
                3 -> 8
                else -> 0
            }
            val candidatesCount = when (session.id) {
                1 -> 1
                2 -> 2
                3 -> 2
                else -> 0
            }
            val acceptedCount = when (session.id) {
                1 -> 1
                2 -> 0
                3 -> 2
                else -> 0
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Private notes: $notesCount, Candidate findings: $candidatesCount, Accepted findings: $acceptedCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Last reviewed: ${session.lastReviewedText} • Changed since review: ${if (session.changedSinceReview) "Yes" else "No"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

@Composable
fun SampleTemplateItem(title: String, fileName: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("sample_template_$fileName"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = MaterialTheme.shapes.medium,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = "Doc Spec",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = fileName,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = "Start spec session",
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp).minimumInteractiveComponentSize()
            )
        }
    }
}
