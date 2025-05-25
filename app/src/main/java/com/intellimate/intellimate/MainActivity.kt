package com.intellimate.intellimate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import android.content.Intent
import android.net.Uri
import android.app.Activity
import android.content.Context
import android.media.projection.MediaProjectionManager
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.intellimate.intellimate.data.repository.StylePreferencesRepository
import com.intellimate.intellimate.ui.features.history.HistoryScreen
import com.intellimate.intellimate.ui.features.knowledge.KnowledgeBaseScreen
import com.intellimate.intellimate.ui.features.matches.HighPotentialMatchesScreen
import com.intellimate.intellimate.ui.features.practice.PracticeModeScreen // Import PracticeModeScreen
import com.intellimate.intellimate.ui.features.settings.StyleSettingsScreen
import com.intellimate.intellimate.ui.theme.IntelliMateTheme
import com.intellimate.intellimate.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IntelliMateTheme {
                var showStyleSettings by remember { mutableStateOf(false) }
                var showKnowledgeBase by remember { mutableStateOf(false) }
                var showHistoryScreen by remember { mutableStateOf(false) }
                var showSavedMatchesScreen by remember { mutableStateOf(false) }
                var showPracticeModeScreen by remember { mutableStateOf(false) } // State for Practice Mode
                val context = LocalContext.current
                val mainViewModel: MainViewModel = viewModel()
                val practiceViewModel: PracticeViewModel = viewModel() // Get PracticeViewModel

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when {
                        showStyleSettings -> {
                            StyleSettingsScreen(
                                stylePreferencesRepository = StylePreferencesRepository(context),
                                onClose = { showStyleSettings = false }
                            )
                        }
                        showKnowledgeBase -> {
                            KnowledgeBaseScreen(
                                mainViewModel = mainViewModel,
                                onClose = { showKnowledgeBase = false }
                            )
                        }
                        showHistoryScreen -> {
                            HistoryScreen(
                                mainViewModel = mainViewModel,
                                onClose = { showHistoryScreen = false }
                            )
                        }
                        showSavedMatchesScreen -> {
                            HighPotentialMatchesScreen(
                                mainViewModel = mainViewModel,
                                onClose = { showSavedMatchesScreen = false }
                            )
                        }
                        showPracticeModeScreen -> { // Condition for Practice Mode Screen
                            PracticeModeScreen(
                                practiceViewModel = practiceViewModel,
                                onClose = { showPracticeModeScreen = false }
                            )
                        }
                        else -> {
                            MainScreenContent(
                                modifier = Modifier.padding(innerPadding),
                                mainViewModel = mainViewModel,
                                onNavigateToStyleSettings = { showStyleSettings = true },
                                onNavigateToKnowledgeBase = { showKnowledgeBase = true },
                                onNavigateToHistory = { showHistoryScreen = true },
                                onNavigateToSavedMatches = { showSavedMatchesScreen = true },
                                onNavigateToPracticeMode = { showPracticeModeScreen = true } // Pass new callback
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreenContent(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = viewModel(),
    onNavigateToStyleSettings: () -> Unit,
    onNavigateToKnowledgeBase: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToSavedMatches: () -> Unit,
    onNavigateToPracticeMode: () -> Unit // New navigation callback
) {
    val context = LocalContext.current
    val suggestion by mainViewModel.suggestion.collectAsState()
    val isLoading by mainViewModel.isLoading.collectAsState()
    val contextEntries by mainViewModel.contextEntries.collectAsState()
    val currentAnalysis by mainViewModel.currentDialogueAnalysis.collectAsState() // Collect analysis
    var inputText by remember { mutableStateOf("Hi there! How's your day?") }
    val dateFormatter = remember { SimpleDateFormat("HH:mm:ss dd/MM", Locale.getDefault()) }


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "IntelliMate Dashboard",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(onClick = onNavigateToStyleSettings) {
                Text("AI Style Settings")
            }
            Button(onClick = onNavigateToKnowledgeBase) {
                Text("Knowledge Base")
            }
            Button(onClick = onNavigateToHistory) {
                Text("View History")
            }
            Button(onClick = onNavigateToSavedMatches) {
                Text("Saved Matches")
            }
            Button(onClick = onNavigateToPracticeMode) { // Button for Practice Mode
                Text("Practice Mode")
            }
        }

    // Current Dialogue Insights Section (New)
    currentAnalysis?.let { analysis ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Current Dialogue Insights (Mock)", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                StatusRow(
                    label = "Current Vibe:",
                    status = "${getEmotionEmoji(analysis.emotion?.primaryEmotion)} ${analysis.emotion?.primaryEmotion?.replaceFirstChar { it.titlecase() } ?: "N/A"}"
                )
                StatusRow(
                    label = "Perceived Intent:",
                    status = analysis.intent?.primaryIntent?.replaceFirstChar { it.titlecase() }?.replace("_", " ") ?: "N/A"
                )
                // Display Openness Score if applicable
                val currentStrategy = mainViewModel.stylePreferences.collectAsState().value.datingStrategy
                if (currentStrategy == com.intellimate.intellimate.core.model.DatingStrategy.ACCELERATED_CONNECTION && analysis.opennessScore != null) {
                    StatusRow(
                        label = "Openness Score (Mock):",
                        status = "${analysis.opennessScore}/100"
                    )
                }
            }
        }
    }

        // Service Status Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Service Status", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                StatusRow("Overlay Service:", "Tap 'Start' (Not live tracked)")
                StatusRow("Accessibility Service:", "Check device settings (Not live tracked)")
            }
        }

        // Overlay Controls Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Overlay Controls", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                Button(onClick = {
                    if (Settings.canDrawOverlays(context)) {
                        context.startService(Intent(context, com.intellimate.intellimate.core.system.OverlayService::class.java))
                    } else {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    }
                }) {
                    Text("Start Overlay Service")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    context.stopService(Intent(context, com.intellimate.intellimate.core.system.OverlayService::class.java))
                }) {
                    Text("Stop Overlay Service")
                }
            }
        }

        // Permissions Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Permissions", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                val mediaProjectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
                val screenCaptureLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        Log.i("MainActivity", "MediaProjection token acquired successfully.")
                    } else {
                        Log.w("MainActivity", "Screen capture permission denied.")
                    }
                }
                Button(onClick = {
                    screenCaptureLauncher.launch(mediaProjectionManager.createScreenCaptureIntent())
                }) {
                    Text("Request Screen Capture Permission")
                }
            }
        }

        // AI Suggestion Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("AI Interaction", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    label = { Text("Input for AI Suggestion") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    mainViewModel.fetchDemoSuggestion(inputText)
                }, enabled = !isLoading) {
                    Text("Get Mock AI Suggestion")
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        text = "AI Suggestion: $suggestion",
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    // Display Dialogue Analysis
                    currentAnalysis?.let { analysis ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                    append("Mock Emotion: ")
                                }
                                append("${analysis.emotion?.primaryEmotion ?: "N/A"} (Intensity: ${analysis.emotion?.intensity?.let { "%.2f".format(it) } ?: "N/A"})")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                    append("Mock Intent: ")
                                }
                                append("${analysis.intent?.primaryIntent ?: "N/A"} (Confidence: ${analysis.intent?.confidence?.let { "%.2f".format(it) } ?: "N/A"})")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                    append("Mock Keywords: ")
                                }
                                append(analysis.semantics?.keywords?.joinToString(", ") ?: "N/A")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.fillMaxWidth()
                        )
                        analysis.semantics?.summary?.let { summary ->
                             Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                        append("Mock Summary: ")
                                    }
                                    append(summary)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Context Entries Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Recent Context Entries", style = MaterialTheme.typography.titleMedium)
                    Row {
                        IconButton(onClick = { mainViewModel.loadLatestContextEntries() }) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Refresh Context")
                        }
                        IconButton(onClick = { mainViewModel.clearAllContextEntries() }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Clear All Context")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (contextEntries.isEmpty()) {
                    Text("No context entries yet. Interact with a tracked app.", style = MaterialTheme.typography.bodySmall)
                } else {
                    LazyColumn(modifier = Modifier.height(150.dp)) { // Limiting height for demo
                        items(contextEntries) { entry ->
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                Text(
                                    text = "${entry.sourceAppPackage} (${dateFormatter.format(Date(entry.timestamp))})",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(entry.message, style = MaterialTheme.typography.bodySmall)
                                Divider(modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusRow(label: String, status: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Info, contentDescription = "Status Info", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.weight(1f))
        Text(text = status, style = MaterialTheme.typography.bodyMedium)
    }
}

// Helper function for emotion to emoji mapping (can be moved to a utils file later)
fun getEmotionEmoji(emotion: String?): String {
    return when (emotion?.lowercase()) {
        "happy", "joyful", "excited" -> "😊"
        "curious" -> "🤔"
        "sad", "frustrated", "angry" -> "😟"
        "surprised" -> "😮"
        "neutral" -> "💬"
        else -> "💬" // Default
    }
}


@Preview(showBackground = true)
@Composable
fun MainScreenContentPreview() {
    IntelliMateTheme {
        MainScreenContent(
            mainViewModel = MainViewModel(Application()),
            onNavigateToStyleSettings = {},
            onNavigateToKnowledgeBase = {},
            onNavigateToHistory = {},
            onNavigateToSavedMatches = {},
            onNavigateToPracticeMode = {} // Added for preview
        )
    }
}
