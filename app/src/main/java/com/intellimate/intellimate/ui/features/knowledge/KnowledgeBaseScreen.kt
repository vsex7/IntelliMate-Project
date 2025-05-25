package com.intellimate.intellimate.ui.features.knowledge

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intellimate.intellimate.data.local.model.RagKnowledgeSnippet
import com.intellimate.intellimate.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KnowledgeBaseScreen(
    mainViewModel: MainViewModel, // Assuming MainViewModel is already set up to handle RAG
    onClose: () -> Unit
) {
    val snippets by mainViewModel.ragSnippets.collectAsState() // Assuming ragSnippets StateFlow in ViewModel
    var newSnippetText by remember { mutableStateOf("") }
    val dateFormatter = remember { SimpleDateFormat("HH:mm dd/MM/yy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Knowledge Base (RAG)") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Close Knowledge Base")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                "Add and view snippets that the AI can reference for more personalized suggestions.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newSnippetText,
                    onValueChange = { newSnippetText = it },
                    label = { Text("New knowledge snippet") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (newSnippetText.isNotBlank()) {
                            mainViewModel.addRagSnippet(newSnippetText)
                            newSnippetText = "" // Clear input
                        }
                    },
                    enabled = newSnippetText.isNotBlank()
                ) {
                    Icon(Icons.Filled.AddCircle, contentDescription = "Add Snippet", modifier = Modifier.size(36.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Stored Snippets:", style = MaterialTheme.typography.titleMedium)
            if (snippets.isEmpty()) {
                Text(
                    "No snippets added yet.",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(snippets) { snippet ->
                        SnippetItem(snippet, dateFormatter)
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun SnippetItem(snippet: RagKnowledgeSnippet, dateFormatter: SimpleDateFormat) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(snippet.text, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Added: ${dateFormatter.format(Date(snippet.timestamp))}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
