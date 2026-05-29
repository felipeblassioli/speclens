package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddPrivateNoteSheet
import com.example.ui.components.TriageOrEditFindingSheet
import com.example.ui.screens.PublishScreen
import com.example.ui.screens.ReviewScreen
import com.example.ui.screens.SessionsScreen
import com.example.ui.screens.TriageScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NavigationTab
import com.example.ui.viewmodel.SpecLensViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                SpecLensApp()
            }
        }
    }
}

@Composable
fun SpecLensApp() {
    val viewModel: SpecLensViewModel = viewModel()

    // Collect States
    val currentTab by viewModel.currentTab.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()
    val activeSession by viewModel.currentSession.collectAsState()
    val notes by viewModel.notesForCurrentSession.collectAsState()
    val findings by viewModel.findingsForCurrentSession.collectAsState()

    // Sheet states
    val showAddNoteSheet by viewModel.showAddNoteSheet.collectAsState()
    val addNoteContext by viewModel.addNoteLineContext.collectAsState()
    val showTriageNoteSheet by viewModel.showTriageNoteSheet.collectAsState()
    val showFindingEditSheet by viewModel.showFindingEditSheet.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("speclens_bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ) {
                // Sessions Tab
                NavigationBarItem(
                    selected = currentTab == NavigationTab.SESSIONS,
                    onClick = { viewModel.setTab(NavigationTab.SESSIONS) },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "Active sessions tab") },
                    label = { Text("Sessions") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.secondary,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_sessions")
                )

                // Review Tab
                NavigationBarItem(
                    selected = currentTab == NavigationTab.REVIEW,
                    onClick = { viewModel.setTab(NavigationTab.REVIEW) },
                    icon = { Icon(imageVector = Icons.Default.RateReview, contentDescription = "Review tab") },
                    label = { Text("Review") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.secondary,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_review")
                )

                // Triage Tab
                NavigationBarItem(
                    selected = currentTab == NavigationTab.TRIAGE,
                    onClick = { viewModel.setTab(NavigationTab.TRIAGE) },
                    icon = { Icon(imageVector = Icons.Default.Checklist, contentDescription = "Triage findings tab") },
                    label = { Text("Triage") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.secondary,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_triage")
                )

                // Publish Tab
                NavigationBarItem(
                    selected = currentTab == NavigationTab.PUBLISH,
                    onClick = { viewModel.setTab(NavigationTab.PUBLISH) },
                    icon = { Icon(imageVector = Icons.Default.IosShare, contentDescription = "Publish and export tab") },
                    label = { Text("Publish") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.secondary,
                        selectedTextColor = MaterialTheme.colorScheme.secondary,
                        indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_publish")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            when (currentTab) {
                NavigationTab.SESSIONS -> {
                    SessionsScreen(
                        viewModel = viewModel,
                        sessions = sessions,
                        onStartCustomSession = { title, file ->
                            viewModel.createCustomSession(title, file)
                        }
                    )
                }
                NavigationTab.REVIEW -> {
                    ReviewScreen(
                        viewModel = viewModel,
                        session = activeSession,
                        notes = notes
                    )
                }
                NavigationTab.TRIAGE -> {
                    TriageScreen(
                        viewModel = viewModel,
                        session = activeSession,
                        notes = notes,
                        findings = findings
                    )
                }
                NavigationTab.PUBLISH -> {
                    PublishScreen(
                        viewModel = viewModel,
                        session = activeSession,
                        notes = notes,
                        findings = findings
                    )
                }
            }
        }
    }

    // Overlay Bottom Sheets Dialog components
    if (showAddNoteSheet) {
        AddPrivateNoteSheet(
            viewModel = viewModel,
            lineContext = addNoteContext,
            onDismiss = { viewModel.closeAddNoteSheet() }
        )
    }

    if (showTriageNoteSheet != null) {
        TriageOrEditFindingSheet(
            viewModel = viewModel,
            noteToPromote = showTriageNoteSheet,
            findingToEdit = null,
            onDismiss = { viewModel.closeTriageNoteSheet() }
        )
    }

    if (showFindingEditSheet != null) {
        TriageOrEditFindingSheet(
            viewModel = viewModel,
            noteToPromote = null,
            findingToEdit = showFindingEditSheet,
            onDismiss = { viewModel.closeFindingEditSheet() }
        )
    }
}
