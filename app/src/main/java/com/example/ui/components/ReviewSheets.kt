package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.database.Finding
import com.example.data.database.PrivateNote
import com.example.ui.theme.*
import com.example.ui.viewmodel.SpecLensViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPrivateNoteSheet(
    viewModel: SpecLensViewModel,
    lineContext: Pair<String, Int>?,
    onDismiss: () -> Unit
) {
    if (lineContext == null) return

    val filePath = lineContext.first
    val lineNum = lineContext.second

    var noteType by remember { mutableStateOf("Concern") }
    var severityGuess by remember { mutableStateOf("Blocking") }
    var content by remember { mutableStateOf("") }
    var sectionName by remember { mutableStateOf("Data Flow") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(MaterialTheme.shapes.medium)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "note draw", tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Add Private Note",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Linked Metadata
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "linked: $sectionName",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Text(text = "•", color = MaterialTheme.colorScheme.outlineVariant)
                            Text(
                                text = "anchor: $filePath : line $lineNum",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.minimumInteractiveComponentSize()
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "close note dialog")
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // noteType selector row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "NOTE TYPE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Concern", "Question", "Trade-off", "Clarity").forEach { type ->
                            val selected = noteType == type
                            val bg = if (selected) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerLow
                            val border = if (selected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                            val textCol = if (selected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .background(bg)
                                    .border(1.dp, border, MaterialTheme.shapes.small)
                                    .clickable { noteType = type }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                                    .testTag("note_type_chip_$type")
                            ) {
                                Text(text = type, style = MaterialTheme.typography.labelMedium, color = textCol)
                            }
                        }
                    }
                }

                // severityGuess selector row
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "SEVERITY GUESS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Blocking", "Major", "Minor", "Nit", "None").forEach { sev ->
                            val selected = severityGuess == sev
                            val bg = if (selected) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerLow
                            val border = if (selected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant
                            val textCol = if (selected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant

                            Box(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .background(bg)
                                    .border(1.dp, border, MaterialTheme.shapes.small)
                                    .clickable { severityGuess = sev }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("severity_chip_$sev")
                            ) {
                                Text(text = sev, style = MaterialTheme.typography.labelMedium, color = textCol)
                            }
                        }
                    }
                }

                // Note Content Text Area
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "NOTE CONTENT", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Markdown supported", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    }

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = { Text("Write your detailed review considerations here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("add_note_content_textfield"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )
                }

                // Dynamic selector Section Anchor
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "Section Anchor", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = sectionName,
                        onValueChange = { sectionName = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }

                // Privacy Alert reminder card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), MaterialTheme.shapes.small)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "lock info logo", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
                        Column {
                            Text(text = "Private note.", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                            Text(
                                text = "This will not appear in final output exported documents unless explicitly promoted during triage panels.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Action CTAs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            viewModel.addPrivateNote(
                                sectionName = sectionName,
                                filePath = filePath,
                                lineNumber = lineNum,
                                noteType = noteType,
                                severityGuess = severityGuess,
                                content = content
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("save_private_note_doc_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = "save private comment", modifier = Modifier.size(16.dp))
                            Text("Save Private Note")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TriageOrEditFindingSheet(
    viewModel: SpecLensViewModel,
    noteToPromote: PrivateNote?,
    findingToEdit: Finding?,
    onDismiss: () -> Unit
) {
    if (noteToPromote == null && findingToEdit == null) return

    var title by remember { mutableStateOf("") }
    var linkedSection by remember { mutableStateOf("") }
    var severity by remember { mutableStateOf("Blocking") }
    var evidenceInternal by remember { mutableStateOf("") }
    var draftWording by remember { mutableStateOf("") }
    var triageDecision by remember { mutableStateOf(false) } // false = Candidate, true = Accepted

    // Prepopulate fields
    LaunchedEffect(noteToPromote, findingToEdit) {
        if (noteToPromote != null) {
            title = "Strict ${noteToPromote.sectionName} validation is undefined"
            linkedSection = noteToPromote.sectionName
            severity = noteToPromote.severityGuess.ifEmpty { "Blocking" }
            evidenceInternal = noteToPromote.content
            draftWording = "The design leaves strict validation in the middleware path. Before proceeding, we should specify recovery paths and strict validation parameters."
            triageDecision = false
        } else if (findingToEdit != null) {
            title = findingToEdit.title
            linkedSection = findingToEdit.linkedSection
            severity = findingToEdit.severity
            evidenceInternal = findingToEdit.evidenceInternal
            draftWording = findingToEdit.draftWording
            triageDecision = findingToEdit.isAccepted
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .clip(MaterialTheme.shapes.medium)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = if (noteToPromote != null) "Promote to Finding" else "Edit Candidate Finding",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "STATUS:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                            Box(
                                modifier = Modifier
                                    .clip(androidx.compose.foundation.shape.CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f))
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), androidx.compose.foundation.shape.CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (triageDecision) "ACCEPTED" else "CANDIDATE",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (triageDecision) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "close find edit")
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Title field code
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = "TITLE", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth().testTag("finding_title_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )
                }

                // Linked Section & Severity double layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "LINKED SECTION", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        OutlinedTextField(
                            value = linkedSection,
                            onValueChange = { linkedSection = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1.2f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "SEVERITY", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("Blocking", "Major", "Minor", "Nit").forEach { sev ->
                                val active = severity == sev
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(if (active) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainerLow)
                                        .border(1.dp, if (active) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                                        .clickable { severity = sev }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = sev,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = if (active) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Evidence Quote Internal
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "EVIDENCE (INTERNAL)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(text = "Markdown supported", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    }
                    OutlinedTextField(
                        value = evidenceInternal,
                        onValueChange = { evidenceInternal = it },
                        modifier = Modifier.fillMaxWidth().height(72.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )
                }

                // Draft wording to publish
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "DRAFT WORDING (PUBLIC COMMENT)", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable {
                                // magic auto-refine action updates the description cleanly!
                                draftWording = "Architectural review feedback regarding $linkedSection:\n\n$draftWording\n\nRecommendation: Please document specific fallback mechanics securely to prevent deadlock storms."
                            }
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Auto Refine icon", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Auto-refine", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    OutlinedTextField(
                        value = draftWording,
                        onValueChange = { draftWording = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("draft_public_word_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        )
                    )
                }

                // Triage Decisions Selector Box
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "TRIAGE DECISION", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Keep Candidate choice
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(if (!triageDecision) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerLow)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                            .clickable { triageDecision = false }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = !triageDecision,
                            onClick = { triageDecision = false }
                        )
                        Text(
                            text = "Keep as Candidate and review later",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Promote Direct Accepted choice
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.small)
                            .background(if (triageDecision) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerLow)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                            .clickable { triageDecision = true }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RadioButton(
                            selected = triageDecision,
                            onClick = { triageDecision = true },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.secondary)
                        )
                        Text(
                            text = "Accept and include in final publish output draft report",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Privacy reminder footer info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = "hint", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Only structured accepted findings will appear in final report.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                }

                // CTA row actions
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss, modifier = Modifier.minimumInteractiveComponentSize()) {
                        Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (noteToPromote != null) {
                                viewModel.promoteNoteToFinding(
                                    noteId = noteToPromote.id,
                                    title = title,
                                    severity = severity,
                                    linkedSection = linkedSection,
                                    evidenceInternal = evidenceInternal,
                                    draftWording = draftWording,
                                    isAccepted = triageDecision
                                )
                            } else if (findingToEdit != null) {
                                viewModel.updateFinding(
                                    findingId = findingToEdit.id,
                                    title = title,
                                    linkedSection = linkedSection,
                                    severity = severity,
                                    evidenceInternal = evidenceInternal,
                                    draftWording = draftWording,
                                    isAccepted = triageDecision,
                                    isDiscarded = false
                                )
                            }
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("save_finding_decision_btn")
                    ) {
                        Text("Save Changes", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}
