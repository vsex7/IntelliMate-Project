package com.intellimate.intellimate.ui.features.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.intellimate.intellimate.core.model.DatingStrategy // Import DatingStrategy
import com.intellimate.intellimate.data.repository.StylePreferences
import com.intellimate.intellimate.data.repository.StylePreferencesRepository
import kotlinx.coroutines.launch
import com.intellimate.intellimate.data.repository.StyleDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown

@Composable
fun StyleSettingsScreen(
    stylePreferencesRepository: StylePreferencesRepository,
    onClose: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val currentPrefs by stylePreferencesRepository.stylePreferencesFlow.collectAsState(
        initial = StylePreferences(
            formality = StyleDefaults.FORMALITY,
            humorLevel = StyleDefaults.HUMOR_LEVEL,
            enthusiasm = StyleDefaults.ENTHUSIASM,
            datingStrategy = StyleDefaults.DATING_STRATEGY // Include default strategy
        )
    )

    // Formality options
    val formalityOptions = listOf("Low", "Medium", "High")
    var formalityExpanded by remember { mutableStateOf(false) }

    // Enthusiasm options
    val enthusiasmOptions = listOf("Calm", "Neutral", "Enthusiastic")

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Customize AI Style", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(bottom = 24.dp))

            // Formality Dropdown
            Text("Formality", style = MaterialTheme.typography.titleMedium)
            Box {
                OutlinedButton(
                    onClick = { formalityExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(currentPrefs.formality)
                    Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Formality")
                }
                DropdownMenu(
                    expanded = formalityExpanded,
                    onDismissRequest = { formalityExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    formalityOptions.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                scope.launch { stylePreferencesRepository.updateFormality(selectionOption) }
                                formalityExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Humor Level Slider
            Text("Humor Level: ${String.format("%.1f", currentPrefs.humorLevel)}", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = currentPrefs.humorLevel,
                onValueChange = {
                    // This updates the state for the Slider immediately
                    // The actual saving to DataStore happens onValueChangeFinished
                    // For simplicity, we can save directly or use a temporary state holder
                    scope.launch { stylePreferencesRepository.updateHumorLevel(it) }
                },
                valueRange = 0f..1f,
                steps = 9, // 0.0, 0.1, ..., 1.0
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Enthusiasm Radio Buttons
            Text("Enthusiasm", style = MaterialTheme.typography.titleMedium)
            Column {
                enthusiasmOptions.forEach { option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp) // Reduced padding
                            .clickable { scope.launch { stylePreferencesRepository.updateEnthusiasm(option) } },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == currentPrefs.enthusiasm),
                            onClick = { scope.launch { stylePreferencesRepository.updateEnthusiasm(option) } }
                        )
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Dating Strategy Radio Buttons
            Text("Dating Strategy", style = MaterialTheme.typography.titleMedium)
            Column {
                DatingStrategy.entries.forEach { strategy -> // Use entries for enums (Kotlin 1.9+) or values()
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp) // Reduced padding
                            .clickable { scope.launch { stylePreferencesRepository.updateDatingStrategy(strategy) } },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (strategy == currentPrefs.datingStrategy),
                            onClick = { scope.launch { stylePreferencesRepository.updateDatingStrategy(strategy) } }
                        )
                        Text(
                            text = strategy.displayName, // Use displayName
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Push button to bottom
            Button(onClick = onClose, modifier = Modifier.align(Alignment.End)) {
                Text("Close")
            }
        }
    }
}
