package com.intellimate.intellimate.ui.features.matches

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.intellimate.intellimate.data.local.model.PotentialMatch
import com.intellimate.intellimate.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HighPotentialMatchesScreen(
    mainViewModel: MainViewModel,
    onClose: () -> Unit
) {
    val matches by mainViewModel.highPotentialMatches.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Saved Potential Matches") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Close Saved Matches")
                    }
                },
                actions = {
                    if (matches.isNotEmpty()) {
                        TextButton(onClick = {
                            // Confirmation dialog would be good here in a real app
                            scope.launch { mainViewModel.clearAllPotentialMatches() }
                        }) {
                            Text("Clear All")
                        }
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
            if (matches.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No potential matches saved yet.")
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(matches, key = { it.profileId }) { match ->
                        PotentialMatchItem(
                            match = match,
                            dateFormatter = dateFormatter,
                            onDelete = { mainViewModel.deletePotentialMatch(match.profileId) }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
fun PotentialMatchItem(
    match: PotentialMatch,
    dateFormatter: SimpleDateFormat,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(match.userGivenName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDelete, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Match", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("App: ${match.sourceAppPackage}", style = MaterialTheme.typography.bodySmall)
            Text("Strategy: ${match.strategyWhenSaved}", style = MaterialTheme.typography.bodySmall)
            match.potentialScore?.let {
                Text("Score: $it/100", style = MaterialTheme.typography.bodySmall)
            }
            match.notes?.let {
                Text("Notes: $it", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Saved: ${dateFormatter.format(Date(match.savedTimestamp))}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
