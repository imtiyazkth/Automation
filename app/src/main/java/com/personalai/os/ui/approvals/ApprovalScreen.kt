package com.personalai.os.ui.approvals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personalai.os.ui.components.AppCard
import com.personalai.os.ui.components.ApprovalCardContent
import com.personalai.os.ui.components.EmptyState
import com.personalai.os.ui.components.ScreenHeader
import com.personalai.os.ui.components.fadeEdges
import com.personalai.os.ui.theme.AppTheme

@Composable
fun ApprovalScreen(viewModel: ApprovalViewModel, onOpenChat: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()
    val c = AppTheme.colors
    val count = state.pending.size

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Approvals",
            subtitle = when (count) {
                0 -> null
                1 -> "1 item needs your decision"
                else -> "$count items need your decision"
            }
        )

        if (count == 0 && state.lastResult == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Default.CheckCircle,
                    title = "Nothing to approve",
                    body = "When an agent drafts a message or wants to take a sensitive step, it shows up here first.",
                    actionLabel = "Ask the assistant",
                    onAction = onOpenChat
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fadeEdges(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                state.lastResult?.let { result ->
                    item(key = "last-result") {
                        AppCard(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Last result", style = MaterialTheme.typography.labelMedium, color = c.textMuted)
                                Text(result, style = MaterialTheme.typography.bodyMedium, color = c.text, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }
                }
                items(state.pending, key = { it.id }) { approval ->
                    ApprovalCardContent(
                        summary = approval.draftSummary,
                        reason = approval.reason,
                        message = approval.step.params["message"] as? String,
                        canEdit = true,
                        onSend = { viewModel.send(approval.id) },
                        onSendEdited = { viewModel.sendEdited(approval.id, it) },
                        onIgnore = { viewModel.ignore(approval.id) }
                    )
                }
            }
        }
    }
}
