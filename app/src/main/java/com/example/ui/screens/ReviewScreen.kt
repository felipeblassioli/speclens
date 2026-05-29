package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Commit
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.PrivateNote
import com.example.data.database.ReviewSession
import com.example.ui.theme.*
import com.example.ui.viewmodel.SpecLensViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: SpecLensViewModel,
    session: ReviewSession?,
    notes: List<PrivateNote>
) {
    if (session == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Select a Review Session from the Sessions Tab.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedCategoryForComposer by remember { mutableStateOf("Concern") }
    var noteContentInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .border(BorderStroke(0.dp, Color.Transparent))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Review Document",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = session.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // File path container
                    Box(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = session.fileName,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Hash tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Commit,
                            contentDescription = "Snapshot Commit",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "@${session.snapshot}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Private Mode Warning Tag
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.15f))
                            .border(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = "Private Badge",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Private review",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        },
        bottomBar = {
            // Note Composer Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding() // Safe drawing block
            ) {
                // Category Selector chips row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val categories = listOf("Concern", "Question", "Trade-off", "Clarity")
                    categories.forEach { category ->
                        val isSelected = selectedCategoryForComposer == category
                        val chipBgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
                        val chipTextColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        val chipBorderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant

                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(chipBgColor)
                                .border(1.dp, chipBorderColor, CircleShape)
                                .clickable { selectedCategoryForComposer = category }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .testTag("composer_chip_$category")
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelLarge,
                                color = chipTextColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Input area with action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = noteContentInput,
                        onValueChange = { noteContentInput = it },
                        placeholder = { Text("Capture a private note...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("composer_note_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send)
                    )

                    IconButton(
                        onClick = {
                            if (noteContentInput.isNotBlank()) {
                                viewModel.addPrivateNote(
                                    sectionName = "Data Flow", // standard linked anchor for session markdown
                                    filePath = session.fileName,
                                    lineNumber = 6, // simulated line
                                    noteType = selectedCategoryForComposer,
                                    severityGuess = "Blocking",
                                    content = noteContentInput
                                )
                                noteContentInput = ""
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("submit_captured_note")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Capture Private Note",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Privacy helper info text
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info metadata",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Private notes stay out of the final review unless promoted.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Render beautiful Markdown spec doc segments
            val lines = session.markdownContent.split("\n")
            var inCodeBlock = false
            val currentCodeLines = mutableListOf<String>()

            lines.forEach { line ->
                if (line.trim().startsWith("```")) {
                    inCodeBlock = !inCodeBlock
                    if (!inCodeBlock) {
                        // Render full monospaced Code blocks with active comments matching line 6
                        CodeBlockRenderer(
                            filePath = "src/middleware/auth.js",
                            codeLines = currentCodeLines,
                            notes = notes,
                            viewModel = viewModel
                        )
                        currentCodeLines.clear()
                    }
                } else if (inCodeBlock) {
                    currentCodeLines.add(line)
                } else {
                    // Regular markdown styling
                    when {
                        line.startsWith("# ") -> {
                            Text(
                                text = line.removePrefix("# "),
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                        line.startsWith("## ") -> {
                            Text(
                                text = line.removePrefix("## "),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                        line.startsWith("### ") -> {
                            Text(
                                text = line.removePrefix("### "),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        line.startsWith("* ") || line.startsWith("- ") -> {
                            Row(
                                modifier = Modifier.padding(start = 12.dp, bottom = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "•",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = line.substring(2),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        line.isNotBlank() -> {
                            // If contains file indicators, render inline interactive code block directly
                            if (line.contains("auth.js") || line.contains(".kt") || line.contains(".go")) {
                                val simulatedFile = line.trim().removeSurrounding("`")
                                val simulatedCode = when {
                                    simulatedFile.contains("auth.js") -> listOf(
                                        "async function validateToken(req, res, next) {",
                                        "  const token = extractToken(req);",
                                        "  // TODO: Define strict validation behavior here",
                                        "  if (!token) return res.status(401).send();",
                                        "}"
                                    )
                                    simulatedFile.contains(".kt") -> listOf(
                                        "fun routePayment(tx: Transaction): Gateway {",
                                        "  val metrics = gatewayTable.getHealth()",
                                        "  // TODO: Add strict fallback check metrics here",
                                        "  return metrics.bestRoute(tx)",
                                        "}"
                                    )
                                    else -> listOf(
                                        "func ReconcileLedgers(ctx context.Context) {",
                                        "  // TODO: Avoid synchronous production table database locks",
                                        "  batch := statement.FetchNext()",
                                        "}"
                                    )
                                }
                                CodeBlockRenderer(
                                    filePath = simulatedFile,
                                    codeLines = simulatedCode,
                                    notes = notes,
                                    viewModel = viewModel
                                )
                            } else {
                                Text(
                                    text = line,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(140.dp))
        }
    }
}

@Composable
fun CodeBlockRenderer(
    filePath: String,
    codeLines: List<String>,
    notes: List<PrivateNote>,
    viewModel: SpecLensViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = filePath,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Lines block
            Column(
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                codeLines.forEachIndexed { index, line ->
                    val lineNum = index + 1
                    val isLine6 = lineNum == 3 // Matches the simulated TODO item in lists

                    val rowBg = if (isLine6) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)
                    } else {
                        Color.Transparent
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(rowBg)
                            .clickable {
                                // clicking on code lines opens "Add private note" bottom sheet
                                viewModel.openAddNoteSheet(filePath, lineNum + 3)
                            }
                            .padding(horizontal = 12.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Num
                        Text(
                            text = (lineNum + 3).toString().padStart(2, ' '),
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isLine6) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.width(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Code line
                        Text(
                            text = line,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isLine6) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // If is line 6 representation, also inline render the saved notes matching this boundary!
                    if (isLine6) {
                        notes.forEach { note ->
                            // Inline Review Note Anchor
                            Box(
                                modifier = Modifier
                                    .padding(start = 48.dp, end = 16.dp, top = 6.dp, bottom = 6.dp)
                                    .clip(MaterialTheme.shapes.medium)
                                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                                    .padding(12.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(MaterialTheme.shapes.small)
                                                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                                                    .border(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f), MaterialTheme.shapes.small)
                                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                                            ) {
                                                Text(
                                                    text = note.noteType.uppercase(),
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                            }

                                            Text(
                                                text = "${note.sectionName} · line ${note.lineNumber}",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }

                                        // Edit trigger button
                                        IconButton(
                                            onClick = { viewModel.openTriageNoteSheet(note) },
                                            modifier = Modifier.size(24.dp).minimumInteractiveComponentSize()
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit private note",
                                                tint = MaterialTheme.colorScheme.outline,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = note.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Lock private indicator",
                                            tint = MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = "Not included in final review unless promoted.",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.outline,
                                            fontStyle = FontStyle.Italic,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
