package com.personalai.os.ui.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.personalai.os.ui.components.AppCard
import com.personalai.os.ui.components.ApprovalCardContent
import com.personalai.os.ui.components.IconAction
import com.personalai.os.ui.components.ScreenHeader
import com.personalai.os.ui.components.StatusPill
import com.personalai.os.ui.components.Tone
import com.personalai.os.ui.components.fadeEdges
import com.personalai.os.ui.components.rememberReducedMotion
import com.personalai.os.ui.components.tappable
import com.personalai.os.ui.theme.AppIcons
import com.personalai.os.ui.theme.AppTheme

private val suggestions = listOf(
    "What can you do?",
    "Who is absent today?",
    "Check this link: https://example.com"
)

private val UserBubbleShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 6.dp, bottomStart = 20.dp)
private val AiBubbleShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 6.dp)

@Composable
fun HeadAgentChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsState()
    val liveState by viewModel.liveState.collectAsState()
    val liveTranscript by viewModel.liveTranscript.collectAsState()
    var input by remember { mutableStateOf("") }
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val c = AppTheme.colors

    val speechLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val heard = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
            if (!heard.isNullOrBlank()) viewModel.send(heard)
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) speechLauncher.launch(buildSpeechIntent()) }

    // Items: 0 = top inset, 1..n = messages, n + 1 = bottom inset.
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size + 1)
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(title = "Head agent")

        if (messages.isEmpty() && liveState == LiveVoiceState.IDLE) {
            WelcomeState(modifier = Modifier.weight(1f), onSuggestionTap = { viewModel.send(it) })
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().fadeEdges(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Box(Modifier.height(4.dp)) }
                items(messages) { msg ->
                    when (msg) {
                        is ChatMessage.Text -> TextBubble(msg)
                        is ChatMessage.ApprovalCard -> ApprovalCardBubble(msg, viewModel)
                    }
                }
                item { Box(Modifier.height(8.dp)) }
            }
        }

        if (liveState != LiveVoiceState.IDLE) {
            LiveVoiceBanner(liveState, liveTranscript)
        }

        // Composer
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.hairline))
        Row(
            Modifier.fillMaxWidth().background(c.background).padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            IconAction(
                icon = AppIcons.Mic,
                contentDescription = "Dictate a message",
                onClick = {
                    val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED
                    if (hasMic) speechLauncher.launch(buildSpeechIntent())
                    else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            )

            val fieldShape = RoundedCornerShape(22.dp)
            Box(
                Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .clip(fieldShape)
                    .background(c.card)
                    .border(1.dp, c.hairline, fieldShape)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    value = input,
                    onValueChange = { input = it },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.text),
                    cursorBrush = SolidColor(c.accent),
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (input.isEmpty()) {
                            Text("Message your agent", style = MaterialTheme.typography.bodyLarge, color = c.textMuted)
                        }
                        inner()
                    }
                )
            }

            if (input.isNotBlank()) {
                Box(
                    Modifier
                        .size(44.dp)
                        .tappable(onClick = { viewModel.send(input); input = "" })
                        .clip(CircleShape)
                        .background(c.accent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", modifier = Modifier.size(20.dp), tint = c.onAccent)
                }
            } else {
                HoldToTalkButton(viewModel, liveState) {
                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }
}

@Composable
private fun HoldToTalkButton(viewModel: ChatViewModel, liveState: LiveVoiceState, onNeedsPermission: () -> Unit) {
    val c = AppTheme.colors
    val reduced = rememberReducedMotion()
    val listening = liveState != LiveVoiceState.IDLE
    val scale by animateFloatAsState(
        targetValue = if (listening && !reduced) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 1f, stiffness = 1200f),
        label = "holdToTalk"
    )
    Box(
        Modifier
            .size(44.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(if (listening) c.danger else c.raised)
            .semantics {
                contentDescription = "Hold to talk"
                role = Role.Button
            }
            .pointerInput(Unit) {
                detectTapGestures(onPress = {
                    if (!viewModel.hasMicPermission()) {
                        onNeedsPermission()
                    } else {
                        viewModel.startLiveVoice()
                        tryAwaitRelease()
                        viewModel.stopLiveVoice()
                    }
                })
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            AppIcons.Waveform, contentDescription = null, modifier = Modifier.size(20.dp),
            tint = if (listening) androidx.compose.ui.graphics.Color.White else c.text
        )
    }
}

@Composable
private fun LiveVoiceBanner(state: LiveVoiceState, transcript: String) {
    val c = AppTheme.colors
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(c.raised)
            .padding(14.dp)
    ) {
        StatusPill(
            if (state == LiveVoiceState.CONNECTING) "Connecting" else "Listening, release to send",
            tone = if (state == LiveVoiceState.CONNECTING) Tone.Warning else Tone.Danger,
            showDot = true
        )
        if (transcript.isNotBlank()) {
            Text(transcript, style = MaterialTheme.typography.bodyMedium, color = c.text, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
private fun WelcomeState(modifier: Modifier = Modifier, onSuggestionTap: (String) -> Unit) {
    val c = AppTheme.colors
    Column(
        modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("What should I take care of?", style = MaterialTheme.typography.titleLarge, color = c.text)
        Text(
            "Ask in plain language. I'll pick the right agent and check with you before anything sensitive goes out.",
            style = MaterialTheme.typography.bodyMedium, color = c.textMuted,
            modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            suggestions.forEach { suggestion ->
                AppCard(Modifier.fillMaxWidth(), onClick = { onSuggestionTap(suggestion) }) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(suggestion, style = MaterialTheme.typography.bodyMedium, color = c.text, modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = c.textMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun TextBubble(msg: ChatMessage.Text) {
    val c = AppTheme.colors
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.fromUser) Arrangement.End else Arrangement.Start
    ) {
        if (msg.fromUser) {
            Box(
                Modifier
                    .widthIn(max = 300.dp)
                    .clip(UserBubbleShape)
                    .background(c.accent)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(msg.text, style = MaterialTheme.typography.bodyLarge, color = c.onAccent)
            }
        } else {
            Box(
                Modifier
                    .widthIn(max = 320.dp)
                    .clip(AiBubbleShape)
                    .background(c.card)
                    .border(1.dp, c.hairline, AiBubbleShape)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(msg.text, style = MaterialTheme.typography.bodyLarge, color = c.text)
            }
        }
    }
}

@Composable
private fun ApprovalCardBubble(msg: ChatMessage.ApprovalCard, viewModel: ChatViewModel) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        ApprovalCardContent(
            summary = msg.summary,
            reason = msg.reason,
            message = msg.editableMessage,
            canEdit = msg.editableMessage != null,
            onSend = { viewModel.approveSend(msg.approvalId) },
            onSendEdited = { viewModel.approveEdited(msg.approvalId, it) },
            onIgnore = { viewModel.ignoreApproval(msg.approvalId) },
            modifier = Modifier.widthIn(max = 340.dp)
        )
    }
}

private fun buildSpeechIntent(): Intent =
    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your command")
    }
