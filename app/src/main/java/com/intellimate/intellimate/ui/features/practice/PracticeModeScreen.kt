package com.intellimate.intellimate.ui.features.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.intellimate.intellimate.core.model.PracticePersona
import com.intellimate.intellimate.viewmodel.PracticeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeModeScreen(
    practiceViewModel: PracticeViewModel,
    onClose: () -> Unit
) {
    val selectedPersona by practiceViewModel.selectedPersona.collectAsState()
    val conversationTranscript by practiceViewModel.conversationTranscript.collectAsState()
    val isSessionActive by practiceViewModel.isSessionActive.collectAsState()
    val personaIsThinking by practiceViewModel.personaIsThinking.collectAsState()

    var userReplyText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll to bottom when new message comes in
    LaunchedEffect(conversationTranscript.size) {
        if (conversationTranscript.isNotEmpty()) {
            listState.animateScrollToItem(conversationTranscript.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dialogue Coach Practice") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Close Practice Mode")
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
            // Controls Section
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PersonaSelector(
                    selectedPersona = selectedPersona,
                    onPersonaSelected = { practiceViewModel.selectPersona(it) },
                    enabled = !isSessionActive || !personaIsThinking // Disable if session active AND persona is thinking
                )
                Button(
                    onClick = { practiceViewModel.startOrRestartSession() },
                    enabled = !personaIsThinking
                ) {
                    Text(if (isSessionActive) "Restart Session" else "Start Session")
                }
            }

            // Conversation Area
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)).padding(8.dp)
            ) {
                items(conversationTranscript) { message ->
                    MessageBubble(
                        sender = message.sender,
                        text = message.text,
                        isUser = message.sender == "User",
                        isFeedback = message.isFeedback
                    )
                }
                if (personaIsThinking && isSessionActive) {
                    item {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp).size(24.dp))
                    }
                }
            }

            // Input Area
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userReplyText,
                    onValueChange = { userReplyText = it },
                    label = { Text("Your reply...") },
                    modifier = Modifier.weight(1f),
                    enabled = isSessionActive && !personaIsThinking,
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (userReplyText.isNotBlank()) {
                            practiceViewModel.sendUserReply(userReplyText)
                            userReplyText = "" // Clear input
                        }
                    },
                    enabled = isSessionActive && userReplyText.isNotBlank() && !personaIsThinking
                ) {
                    Icon(Icons.Filled.Send, contentDescription = "Send Reply")
                }
            }
        }
    }
}

@Composable
fun PersonaSelector(
    selectedPersona: PracticePersona,
    onPersonaSelected: (PracticePersona) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }, enabled = enabled) {
            Text(selectedPersona.toDisplayName())
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Select Persona")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            PracticePersona.entries.forEach { persona ->
                DropdownMenuItem(
                    text = { Text(persona.toDisplayName()) },
                    onClick = {
                        onPersonaSelected(persona)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun MessageBubble(sender: String, text: String, isUser: Boolean, isFeedback: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Text(
            text = if(isUser) "You" else sender,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isFeedback -> MaterialTheme.colorScheme.tertiaryContainer
                    isUser -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.secondaryContainer
                }
            ),
            modifier = Modifier.widthIn(max = 300.dp) // Max width for bubbles
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(10.dp),
                fontStyle = if (isFeedback) FontStyle.Italic else FontStyle.Normal,
                fontSize = if(isFeedback) 13.sp else 15.sp,
                color = if (isFeedback) MaterialTheme.colorScheme.onTertiaryContainer else if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}
