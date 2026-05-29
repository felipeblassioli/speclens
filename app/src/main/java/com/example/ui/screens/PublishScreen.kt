package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.Finding
import com.example.data.database.PrivateNote
import com.example.data.database.ReviewSession
import com.example.ui.theme.*
import com.example.ui.viewmodel.SpecLensViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(
    viewModel: SpecLensViewModel,
    session: ReviewSession?,
    notes: List<PrivateNote>,
    findings: List<Finding>
) {
    if (session == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Select a Review Session from Sessions Tab first.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val accepted = findings.filter { it.isAccepted && !it.isDiscarded }
    val includeStats by viewModel.markdownIncludeSummaryStats.collectAsState()

    var showClipboardSuccessToast by remember { mutableStateOf(false) }

    // Dynamically compile the polished Markdown document review based on active findings
    val generatedMarkdown = remember(session, accepted, includeStats) {
        val sb = StringBuilder()
        sb.append("# Review: ${session.title}\n")
        sb.append("Document: ${session.fileName}\n")
        sb.append("Reviewed snapshot: ${session.snapshot}\n")
        sb.append("Status: ${if (accepted.any { it.severity == "Blocking" }) "Changes requested" else "Approved with findings"}\n\n")

        sb.append("## Summary\n")
        sb.append("The proposed specification has been reviewed locally and privately using SpecLens. ")
        if (accepted.isEmpty()) {
            sb.append("No blocking or major system design anomalies were found. Proceed and implement details gracefully.\n\n")
        } else {
            sb.append("Critical architectural issues and validation gaps were identified. Please address the structural findings detailed below before proceeding to technical implementation.\n\n")
        }

        if (includeStats) {
            sb.append("### Review Metadata Session Stats\n")
            sb.append("* Raw private review notes recorded: ${notes.size}\n")
            sb.append("* Total candidate findings structured: ${findings.size}\n")
            sb.append("* Promoted findings accepted for output: ${accepted.size}\n\n")
        }

        val blocking = accepted.filter { it.severity == "Blocking" }
        if (blocking.isNotEmpty()) {
            sb.append("## Blocking Findings\n")
            blocking.forEachIndexed { idx, finding ->
                sb.append("### ${idx + 1}. ${finding.title}\n")
                sb.append("${finding.draftWording}\n\n")
            }
        }

        val major = accepted.filter { it.severity == "Major" }
        if (major.isNotEmpty()) {
            sb.append("## Major Findings\n")
            major.forEachIndexed { idx, finding ->
                sb.append("### ${idx + 1}. ${finding.title}\n")
                sb.append("${finding.draftWording}\n\n")
            }
        }

        val other = accepted.filter { it.severity != "Blocking" && it.severity != "Major" }
        if (other.isNotEmpty()) {
            sb.append("## Minor / Nit Findings\n")
            other.forEachIndexed { idx, finding ->
                sb.append("### ${idx + 1}. ${finding.title} (${finding.severity})\n")
                sb.append("${finding.draftWording}\n\n")
            }
        }

        sb.append("## Excluded from this review\n")
        sb.append("* Raw personal engineering notes are securely kept private and deleted upon session termination.\n")
        sb.append("* Unpromoted candidate findings represent unverified notes and are omitted from this markdown export.\n")

        sb.toString()
    }

    val wordsCount = generatedMarkdown.split("\\s+".toRegex()).size
    val charactersCount = generatedMarkdown.length

    val configuration = LocalConfiguration.current
    val isTabletLandscape = configuration.screenWidthDp > 720

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isTabletLandscape) {
            // Responsive Tablet/Desktop split view
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Sidebar Options Frame
                PublishSidebar(
                    session = session,
                    acceptedFindingsCount = accepted.size,
                    excludedNotesCount = notes.size,
                    candidateCount = findings.size - accepted.size,
                    includeStats = includeStats,
                    onToggleStats = { viewModel.setIncludeSummaryStats(it) },
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("SpecLens Review", generatedMarkdown)
                        clipboard.setPrimaryClip(clip)
                        scope.launch {
                            showClipboardSuccessToast = true
                            delay(3000)
                            showClipboardSuccessToast = false
                        }
                    },
                    modifier = Modifier.width(320.dp)
                )

                // Canvas Preview Area
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Report Preview Canvas",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .padding(24.dp)
                    ) {
                        ReportOutputTextarea(
                            markdownText = generatedMarkdown,
                            words = wordsCount,
                            chars = charactersCount
                        )
                    }
                }
            }
        } else {
            // Mobile standard vertical scroll stack
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Final Review Summary",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Edit or preview your exported spec findings report below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Doc Context card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(imageVector = Icons.Default.Description, contentDescription = "MD context", tint = MaterialTheme.colorScheme.primary)
                            Text(text = "DOCUMENT CONTEXT", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                                .padding(8.dp)
                        ) {
                            Text(text = session.fileName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "SNAPSHOT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                                Text(text = "@${session.snapshot}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            // Banner tag changes needed
                            val anyBlocking = accepted.any { it.severity == "Blocking" }
                            val bannerColor = if (anyBlocking) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                            val bannerTxt = if (anyBlocking) "Changes requested" else "Approved findings"
                            Box(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.small)
                                    .background(bannerColor)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (anyBlocking) Icons.Default.Warning else Icons.Default.CheckCircle,
                                        contentDescription = "Status context icon",
                                        tint = if (anyBlocking) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = bannerTxt,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = if (anyBlocking) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        // Stats metrics
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Accepted findings in output:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = "${accepted.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Excluded private review notes:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                            Text(text = "${notes.size}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Tweak toggle Include stats
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = includeStats,
                            onCheckedChange = { viewModel.setIncludeSummaryStats(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.secondary,
                                checkedTrackColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier.testTag("include_stats_switch")
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Include summary stats", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // Document Canvas block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(16.dp)
                ) {
                    ReportOutputTextarea(
                        markdownText = generatedMarkdown,
                        words = wordsCount,
                        chars = charactersCount
                    )
                }

                // Action CTAs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            Toast.makeText(context, "Exporting to local Downloads folder...", Toast.LENGTH_SHORT).show()
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .testTag("export_save_file_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = "Export")
                            Text("Export Report", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("SpecLens Review", generatedMarkdown)
                            clipboard.setPrimaryClip(clip)
                            scope.launch {
                                showClipboardSuccessToast = true
                                delay(3000)
                                showClipboardSuccessToast = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier
                            .weight(1.2f)
                            .minimumInteractiveComponentSize()
                            .testTag("copy_markdown_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                            Text("Copy Markdown", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Active notification snackbar popup
        AnimatedVisibility(
            visible = showClipboardSuccessToast,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 88.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(if (isTabletLandscape) 0.5f else 1.0f)
                    .clip(MaterialTheme.shapes.medium)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium),
                color = MaterialTheme.colorScheme.inverseSurface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success copy",
                        tint = MaterialTheme.colorScheme.secondaryContainer
                    )
                    Text(
                        text = "Markdown copied. Private notes excluded.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.inverseOnSurface
                    )
                }
            }
        }
    }
}

@Composable
fun PublishSidebar(
    session: ReviewSession,
    acceptedFindingsCount: Int,
    excludedNotesCount: Int,
    candidateCount: Int,
    includeStats: Boolean,
    onToggleStats: (Boolean) -> Unit,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(imageVector = Icons.Default.Description, contentDescription = "Context icon", tint = MaterialTheme.colorScheme.primary)
                Text(text = "DOCUMENT CONTEXT", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.outline)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                    .padding(10.dp)
            ) {
                Text(text = session.fileName, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "SNAPSHOT", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
                    Text(text = "@${session.snapshot}", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Changes block
                val anyBlocking = acceptedFindingsCount > 0
                val bannerColor = if (anyBlocking) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
                val bannerTxt = if (anyBlocking) "Changes requested" else "Approved findings"
                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(bannerColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = bannerTxt,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (anyBlocking) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Included accepted findings:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "$acceptedFindingsCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Excluded raw review notes:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    Text(text = "$excludedNotesCount", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Excluded candidate findings:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                    Text(text = "$candidateCount", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = includeStats,
                    onCheckedChange = onToggleStats,
                    colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.secondary)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = "Include summary stats", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }

            Button(
                onClick = onCopy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier
                    .fillMaxWidth()
                    .minimumInteractiveComponentSize()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy")
                    Text("Copy Markdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {},
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.IosShare, contentDescription = "Share icon")
                        Text("Share")
                    }
                }
                OutlinedButton(
                    onClick = {},
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.weight(1f).minimumInteractiveComponentSize()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = "Download")
                        Text("Export")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Visibility, contentDescription = "eye logo", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Previewing Output Report", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun ReportOutputTextarea(
    markdownText: String,
    words: Int,
    chars: Int
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Monospace Markdown lines scrollable area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = markdownText,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp,
                modifier = Modifier.testTag("compiled_markdown_report_textarea")
            )
        }

        Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 8.dp))

        // Status bar words and characters counts
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Markdown supported",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            Text(
                text = "$words words  •  $chars characters",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
