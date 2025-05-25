package com.intellimate.intellimate.ui.features.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.intellimate.intellimate.data.local.model.ContextEntry
import com.intellimate.intellimate.data.local.model.SuggestionFeedback
import com.intellimate.intellimate.viewmodel.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    mainViewModel: MainViewModel,
    onClose: () -> Unit
) {
    val feedbackHistory by mainViewModel.suggestionFeedbackHistory.collectAsState()
    val contextHistory by mainViewModel.contextHistory.collectAsState()
    val dateFormatter = remember { SimpleDateFormat("HH:mm:ss dd/MM/yy", Locale.getDefault()) }

    val tabTitles = listOf("Feedback History", "Context History")
    val pagerState = rememberPagerState { tabTitles.size }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History & Analytics") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Close History")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            TabRow(selectedTabIndex = pagerState.currentPage) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(title) }
                    )
                }
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                when (page) {
                    0 -> FeedbackHistoryPage(feedbackHistory, dateFormatter)
                    1 -> ContextHistoryPage(contextHistory, dateFormatter)
                }
            }
        }
    }
}

@Composable
fun FeedbackHistoryPage(feedbackList: List<SuggestionFeedback>, dateFormatter: SimpleDateFormat) {
    if (feedbackList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("No suggestion feedback recorded yet.")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(feedbackList) { feedback ->
            FeedbackItem(feedback, dateFormatter)
            Divider()
        }
    }
}

@Composable
fun ContextHistoryPage(contextList: List<ContextEntry>, dateFormatter: SimpleDateFormat) {
    if (contextList.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("No context history recorded yet.")
        }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(contextList) { entry ->
            ContextItem(entry, dateFormatter)
            Divider()
        }
    }
}

@Composable
fun FeedbackItem(feedback: SuggestionFeedback, dateFormatter: SimpleDateFormat) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "Suggestion: \"${feedback.suggestionText.take(100)}${if (feedback.suggestionText.length > 100) "..." else ""}\"",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Feedback: ${feedback.feedbackType.replaceFirstChar { it.titlecase() }}",
                style = MaterialTheme.typography.bodySmall,
                color = if (feedback.feedbackType == "good") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            Text(
                "Time: ${dateFormatter.format(Date(feedback.timestamp))}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun ContextItem(entry: ContextEntry, dateFormatter: SimpleDateFormat) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "App: ${entry.sourceAppPackage}",
                style = MaterialTheme.typography.labelMedium
            )
            Text(
                "Message: \"${entry.message.take(150)}${if (entry.message.length > 150) "..." else ""}\"",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (entry.mockEmotion != null || entry.mockIntent != null) {
                Text(
                    "Analysis: Emotion=${entry.mockEmotion ?: "N/A"}, Intent=${entry.mockIntent ?: "N/A"}, Keywords=${entry.mockKeywords ?: "N/A"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                "Time: ${dateFormatter.format(Date(entry.timestamp))}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
