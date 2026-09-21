package com.personalai.os.ui.chat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.personalai.os.ui.theme.OsAccent
import com.personalai.os.ui.theme.OsAiBubble
import com.personalai.os.ui.theme.OsOnSurfaceMuted
import com.personalai.os.ui.theme.OsUserBubble
import com.personalai.os.ui.theme.OsWarning

private val suggestions = listOf(
    "What can you do?",
    "Who is absent today?",
    "Check this link: https://example.com"
)

@Composable
fun HeadAgentChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsState()
    var input by remember { mutableStateOf("") }
    val context = LocalContext.current
    val listState = rememberLazyListState()

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

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
    }

    Column(Modifier.fillMaxSize()) {
        if (messages.isEmpty()) {
            WelcomeState(onSuggestionTap = { viewModel.send(it) })
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(Modifier.height(8.dp)) }
                items(messages) { msg ->
                    when (msg) {
                        is ChatMessage.Text -> TextBubble(msg)
                        is ChatMessage.ApprovalCard -> ApprovalCardBubble(msg, viewModel)
                    }
                }
                item { Spacer(Modifier.height(8.dp)) }
            }
        }

        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = {
                    val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                        PackageManager.PERMISSION_GRANTED
                    if (hasMic) speechLauncher.launch(buildSpeechIntent())
                    else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                },
                shape = MaterialTheme.shapes.extraLarge,
                contentPadding = ButtonDefaults.TextButtonContentPadding
            ) { Text("\uD83C\uDFA4") }

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.large,
                placeholder = { Text("Type a command\u2026") }
            )
            Button(
                onClick = { if (input.isNotBlank()) { viewModel.send(input); input = "" } },
                shape = MaterialTheme.shapes.large
            ) { Text("Send") }
        }
    }
}

@Composable
private fun WelcomeState(onSuggestionTap: (String) -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Hi \u2014 I'm your Head Agent.", style = MaterialTheme.typography.titleLarge)
        Text(
            "Ask me anything, or try one of these:",
            style = MaterialTheme.typography.bodyMedium,
            color = OsOnSurfaceMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
        suggestions.forEach { suggestion ->
            OutlinedButton(
                onClick = { onSuggestionTap(suggestion) },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(suggestion, modifier = Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun TextBubble(msg: ChatMessage.Text) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.fromUser) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (msg.fromUser) OsUserBubble else OsAiBubble,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (!msg.fromUser) {
                    Text(
                        "HEAD AGENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = OsAccent,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }
                Text(msg.text, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun ApprovalCardBubble(msg: ChatMessage.ApprovalCard, viewModel: ChatViewModel) {
    var editing by remember { mutableStateOf(false) }
    var editedText by remember { mutableStateOf(msg.editableMessage ?: "") }

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Card(
            modifier = Modifier.widthIn(max = 320.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(Modifier.padding(14.dp)) {
                Text("NEEDS YOUR OK", style = MaterialTheme.typography.labelSmall, color = OsWarning)
                Text(msg.summary, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 2.dp))
                Text(msg.reason, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 2.dp))

                if (editing) {
                    OutlinedTextField(
                        value = editedText,
                        onValueChange = { editedText = it },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        label = { Text("Message") }
                    )
                }

                Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (editing) {
                        Button(onClick = {
                            viewModel.approveEdited(msg.approvalId, editedText)
                            editing = false
                        }) { Text("Send edited") }
                    } else {
                        Button(onClick = { viewModel.approveSend(msg.approvalId) }) { Text("Send") }
                        if (msg.editableMessage != null) {
                            OutlinedButton(onClick = { editing = true }) { Text("Edit") }
                        }
                    }
                    OutlinedButton(onClick = { viewModel.ignoreApproval(msg.approvalId) }) { Text("Ignore") }
                }
            }
        }
    }
}

private fun buildSpeechIntent(): Intent =
    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your command")
    }
