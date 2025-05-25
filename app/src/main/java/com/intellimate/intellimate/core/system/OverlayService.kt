package com.intellimate.intellimate.core.system

import android.annotation.SuppressLint
import android.app.Service
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.core.app.NotificationCompat
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkAdd // Icon for Save Match
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.intellimate.intellimate.MainActivity
import com.intellimate.intellimate.R
import com.intellimate.intellimate.core.ai.dto.DialogueAnalysis // Import DialogueAnalysis
import com.intellimate.intellimate.data.local.AppDatabase
import com.intellimate.intellimate.data.repository.FeedbackRepository
import com.intellimate.intellimate.data.repository.SuggestionRepository
import com.intellimate.intellimate.ui.theme.IntelliMateTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class OverlayService : Service() {

    companion object {
        private const val TAG_OVERLAY = "IntelliMateOverlaySvc" // Standardized TAG
    }
    private val NOTIFICATION_CHANNEL_ID = "IntelliMateOverlayChannel"
    private val NOTIFICATION_ID = 101

    private lateinit var windowManager: WindowManager
    private var overlayView: ComposeView? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentAiOutput = mutableStateOf<com.intellimate.intellimate.core.ai.dto.SuggestionWithAnalysis?>(null)
    private lateinit var params: WindowManager.LayoutParams
    private lateinit var feedbackRepository: FeedbackRepository

    // For ComposeView lifecycle
    private val viewModelStoreOwner = object : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }
    private val lifecycleOwner = object : SavedStateRegistryOwner {
        private val lifecycleRegistry = androidx.lifecycle.LifecycleRegistry(this)
        private val savedStateRegistryController = SavedStateRegistryController.create(this)

        override val lifecycle: Lifecycle
            get() = lifecycleRegistry

        override val savedStateRegistry: SavedStateRegistry
            get() = savedStateRegistryController.savedStateRegistry

        fun performRestore(savedState: android.os.Bundle?) {
            savedStateRegistryController.performRestore(savedState)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        }

        fun performSave(outState: android.os.Bundle) {
            savedStateRegistryController.performSave(outState)
        }
        fun handleLifecycleEvent(event: Lifecycle.Event) {
            lifecycleRegistry.handleLifecycleEvent(event)
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG_OVERLAY, "onCreate: Service creating.")

        createNotificationChannel()
        Log.d(TAG_OVERLAY, "onCreate: Notification channel created.")

        // Initialize FeedbackRepository
        try {
            val feedbackDao = AppDatabase.getDatabase(applicationContext).suggestionFeedbackDao()
            feedbackRepository = FeedbackRepository(feedbackDao)
            Log.i(TAG_OVERLAY, "onCreate: FeedbackRepository initialized successfully.")
        } catch (e: Exception) {
            Log.e(TAG_OVERLAY, "onCreate: Error initializing FeedbackRepository", e)
            // Handle error - perhaps disable feedback functionality
        }

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        }
        Log.d(TAG_OVERLAY, "onCreate: WindowManager layout flag set to: $layoutFlag")

        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutFlag,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 0
        params.y = 100
        Log.d(TAG_OVERLAY, "onCreate: WindowManager.LayoutParams initialized.")

        overlayView = ComposeView(this).apply {
            setViewTreeViewModelStoreOwner(viewModelStoreOwner)
            setViewTreeSavedStateRegistryOwner(lifecycleOwner)
            setViewTreeLifecycleOwner(lifecycleOwner)

            setContent {
                val aiOutputSnapshot by currentAiOutput
                Log.v(TAG_OVERLAY, "ComposeView recomposing. Suggestion: '${aiOutputSnapshot?.suggestion?.take(30)}...', Tip: '${aiOutputSnapshot?.coachingTip}', Explanation: '${aiOutputSnapshot?.explanation?.take(30)}...', Next Topics: ${aiOutputSnapshot?.predictedNextTopics}, Analysis: ${aiOutputSnapshot?.analysis}")
                IntelliMateTheme {
                    DraggableOverlayContent(
                        suggestion = aiOutputSnapshot?.suggestion,
                        coachingTip = aiOutputSnapshot?.coachingTip,
                        explanation = aiOutputSnapshot?.explanation,
                        predictedNextTopics = aiOutputSnapshot?.predictedNextTopics,
                        analysis = aiOutputSnapshot?.analysis, // Pass analysis for Vibe Meter
                        onPositionChanged = { dx, dy ->
                            params.x += dx.toInt()
                            params.y += dy.toInt()
                            try {
                                if (overlayView?.isAttachedToWindow == true) {
                                    windowManager.updateViewLayout(overlayView, params)
                                } else {
                                    Log.w(TAG_OVERLAY, "ComposeView not attached, cannot update layout.")
                                }
                            } catch (e: IllegalArgumentException) {
                                Log.e(TAG_OVERLAY, "Error updating view layout", e)
                            }
                        },
                        onFeedbackClicked = { suggestionTextForFeedback, feedbackType ->
                            handleFeedbackClicked(suggestionTextForFeedback, feedbackType)
                        }
                    )
                }
            }
        }
        lifecycleOwner.performRestore(null)
        lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        Log.d(TAG_OVERLAY, "onCreate: ComposeView created and lifecycle owners set.")

        serviceScope.launch {
            SuggestionRepository.latestAiOutputFlow.collectLatest { aiOutput ->
                Log.i(TAG_OVERLAY, "New AI Output: Suggestion='${aiOutput?.suggestion?.take(30)}...', Tip='${aiOutput?.coachingTip}', Explanation='${aiOutput?.explanation?.take(30)}...', Next Topics: ${aiOutput?.predictedNextTopics}, Analysis: ${aiOutput?.analysis}")
                currentAiOutput.value = aiOutput
            }
        }
        Log.d(TAG_OVERLAY, "onCreate: AI output flow collection started.")

        try {
            windowManager.addView(overlayView, params)
            Log.i(TAG_OVERLAY, "onCreate: OverlayView added to WindowManager.")
        } catch (e: Exception) {
            Log.e(TAG_OVERLAY, "onCreate: Error adding OverlayView to WindowManager.", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        stopForeground(true)
        overlayView?.let {
            if (it.isAttachedToWindow) { // Check if view is still attached before removing
                try {
                    windowManager.removeView(it)
                    Log.i(TAG_OVERLAY, "onDestroy: OverlayView removed from WindowManager.")
                } catch (e: Exception) {
                    Log.e(TAG_OVERLAY, "onDestroy: Error removing OverlayView from WindowManager.", e)
                }
            }
            lifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            viewModelStoreOwner.viewModelStore.clear()
        }
        overlayView = null
        Log.i(TAG_OVERLAY, "onDestroy: Service destroyed, scope cancelled, view removed.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG_OVERLAY, "onStartCommand: Service started (or restarted). Intent: $intent, Flags: $flags, StartId: $startId")

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, pendingIntentFlags)

        val notification: Notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("IntelliMate is Active")
            .setContentText("Providing smart assistance for your dating apps.")
            .setSmallIcon(R.mipmap.ic_launcher) // Replace with actual app icon
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Makes the notification non-dismissible
            .build()

        startForeground(NOTIFICATION_ID, notification)

        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "IntelliMate Overlay Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
            Log.d(TAG_OVERLAY, "createNotificationChannel: Notification channel created.")
        } else {
            Log.d(TAG_OVERLAY, "createNotificationChannel: Not required for this API level (${Build.VERSION.SDK_INT}).")
        }
    }

    private fun handleFeedbackClicked(suggestionText: String, feedbackType: String) {
        Log.i(TAG_OVERLAY, "Feedback received: '$feedbackType' for suggestion: '${suggestionText.take(30)}...'")
        serviceScope.launch {
            try {
                feedbackRepository.addFeedback(suggestionText, feedbackType)
                Toast.makeText(applicationContext, "Feedback '$feedbackType' submitted!", Toast.LENGTH_SHORT).show()
                Log.d(TAG_OVERLAY, "Feedback '$feedbackType' for suggestion '${suggestionText.take(30)}...' saved.")
            } catch (e: Exception) {
                Log.e(TAG_OVERLAY, "Error saving feedback for suggestion: '${suggestionText.take(30)}...'", e)
                Toast.makeText(applicationContext, "Error saving feedback.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG_OVERLAY, "onBind called, returning null as it's not a bound service.")
        return null
    }
}

@Composable
fun DraggableOverlayContent(
    suggestion: String?,
    coachingTip: String?,
    explanation: String?,
    predictedNextTopics: List<String>?,
    analysis: DialogueAnalysis?,
    onPositionChanged: (Float, Float) -> Unit,
    onFeedbackClicked: (suggestionTextForFeedback: String, feedbackType: String) -> Unit,
    onSaveMatchClicked: () -> Unit // New callback for saving a match
) {
    val context = LocalContext.current
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    var showExplanation by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .sizeIn(minWidth = 200.dp, maxWidth = 300.dp, minHeight = 150.dp, maxHeight = 500.dp)
            .wrapContentHeight()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onPositionChanged(dragAmount.x, dragAmount.y)
                }
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "IntelliMate Suggestion",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                // Vibe Meter Emoji
                Text(
                    text = getVibeEmoji(analysis), // Using analysis for vibe
                    fontSize = 20.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            // Display Openness Score if available in analysis
            analysis?.opennessScore?.let { score ->
                Text(
                    text = "Openness: $score/100",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End).padding(end = 4.dp)
                )
            }


            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = suggestion ?: "Waiting for suggestion...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .wrapContentHeight(Alignment.CenterVertically)
            )
            // Action Buttons Row (Feedback and Copy)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    (suggestion ?: "").let { currentSuggestionText ->
                        if (currentSuggestionText.isNotBlank()) {
                            onFeedbackClicked(currentSuggestionText, "good")
                        } else {
                            Toast.makeText(context, "No suggestion to rate.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Icon(Icons.Filled.ThumbUp, contentDescription = "Good Suggestion", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = {
                     (suggestion ?: "").let { currentSuggestionText ->
                        if (currentSuggestionText.isNotBlank()) {
                            onFeedbackClicked(currentSuggestionText, "bad")
                        } else {
                            Toast.makeText(context, "No suggestion to rate.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }) {
                    Icon(Icons.Filled.ThumbDown, contentDescription = "Bad Suggestion", tint = MaterialTheme.colorScheme.error)
                }
                IconButton(onClick = onSaveMatchClicked) { // Save Match Button
                    Icon(Icons.Filled.BookmarkAdd, contentDescription = "Save Match", tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(
                    onClick = {
                        if (!suggestion.isNullOrEmpty()) {
                            val clip = android.content.ClipData.newPlainText("IntelliMate Suggestion", suggestion)
                            clipboardManager.setPrimaryClip(clip)
                            Toast.makeText(context, "Suggestion copied!", Toast.LENGTH_SHORT).show()
                            Log.i(OverlayService.TAG_OVERLAY, "Copy button clicked. Copied: '${suggestion.take(30)}...'")
                        } else {
                            Toast.makeText(context, "No suggestion to copy.", Toast.LENGTH_SHORT).show()
                            Log.d(OverlayService.TAG_OVERLAY, "Copy button clicked, but no suggestion to copy.")
                        }
                    }
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy suggestion", tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Coaching Tip Section
            coachingTip?.let { tip ->
                Divider(modifier = Modifier.padding(vertical = 6.dp))
                Text(
                    text = "💡 Coach's Tip:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = tip,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                )
            }

            // XAI Explanation Section (Toggleable)
            explanation?.let { expl ->
                Divider(modifier = Modifier.padding(vertical = 6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { showExplanation = !showExplanation },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🤔 Why this suggestion?",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Icon(
                        if (showExplanation) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = if (showExplanation) "Hide explanation" else "Show explanation",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (showExplanation) {
                    Text(
                        text = expl,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    )
                }
            }

            // Predicted Next Topics Section
            if (!predictedNextTopics.isNullOrEmpty()) {
                Divider(modifier = Modifier.padding(vertical = 6.dp))
                Text(
                    text = "✨ Next Topic Ideas:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                predictedNextTopics.forEach { topic ->
                    Text(
                        text = "- $topic",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 2.dp)
                    )
                }
            }
        }
    }
}
