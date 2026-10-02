package com.personalai.os.ui.diagnostics

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.personalai.os.ui.components.AppButton
import com.personalai.os.ui.components.AppCard
import com.personalai.os.ui.components.ButtonKind
import com.personalai.os.ui.components.EmptyState
import com.personalai.os.ui.components.IconAction
import com.personalai.os.ui.components.ScreenHeader
import com.personalai.os.ui.components.fadeEdges
import com.personalai.os.ui.theme.AppShapes
import com.personalai.os.ui.theme.AppTheme
import com.personalai.os.ui.theme.AppType

@Composable
fun DiagnosticsScreen(viewModel: DiagnosticsViewModel, onBack: (() -> Unit)? = null) {
    val state by viewModel.state.collectAsState()
    val c = AppTheme.colors
    val selected = state.selectedContent
    var confirmClear by remember { mutableStateOf(false) }

    BackHandler(enabled = selected != null) { viewModel.closeDetail() }

    if (selected != null) {
        Column(Modifier.fillMaxSize()) {
            ScreenHeader(title = "Log", onBack = { viewModel.closeDetail() })
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .clip(AppShapes.card)
                    .background(c.card)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                SelectionContainer {
                    Text(selected, style = AppType.mono, color = c.text)
                }
            }
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Diagnostics",
            subtitle = "Crash and error logs, read on this device.",
            onBack = onBack,
            trailing = { IconAction(Icons.Default.Refresh, "Refresh", onClick = { viewModel.refresh() }) }
        )

        if (state.logFileNames.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Default.Build,
                    title = "No crash logs",
                    body = "Nothing has thrown an uncaught exception. If the app ever crashes, the log appears here.",
                    actionLabel = "Check again",
                    onAction = { viewModel.refresh() }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fadeEdges(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.logFileNames, key = { it }) { name ->
                    AppCard(Modifier.fillMaxWidth(), onClick = { viewModel.open(name) }) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(name, style = MaterialTheme.typography.bodyMedium, color = c.text, modifier = Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = c.textMuted)
                        }
                    }
                }
                item(key = "clear") {
                    AppButton(
                        "Clear all logs", onClick = { confirmClear = true },
                        kind = ButtonKind.Destructive, modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear all logs?") },
            text = { Text("Every saved crash log on this device will be deleted.") },
            confirmButton = {
                TextButton(onClick = { viewModel.clearAll(); confirmClear = false }) { Text("Clear", color = c.danger) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel", color = c.textMuted) }
            },
            containerColor = c.card,
            titleContentColor = c.text,
            textContentColor = c.textMuted
        )
    }
}
