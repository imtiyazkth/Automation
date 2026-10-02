package com.personalai.os.ui.scheduled

import android.text.format.DateUtils
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.personalai.os.data.entities.ScheduledTaskEntity
import com.personalai.os.ui.components.AppButton
import com.personalai.os.ui.components.AppCard
import com.personalai.os.ui.components.AppTextField
import com.personalai.os.ui.components.ButtonKind
import com.personalai.os.ui.components.EmptyState
import com.personalai.os.ui.components.ScreenHeader
import com.personalai.os.ui.components.SegmentedControl
import com.personalai.os.ui.components.StatusPill
import com.personalai.os.ui.components.appSwitchColors
import com.personalai.os.ui.components.fadeEdges
import com.personalai.os.ui.components.rememberReducedMotion
import com.personalai.os.ui.theme.AppShapes
import com.personalai.os.ui.theme.AppTheme

private data class IntervalPreset(val label: String, val minutes: Long)

private val presets = listOf(
    IntervalPreset("15 min", 15),
    IntervalPreset("1 hour", 60),
    IntervalPreset("6 hours", 360),
    IntervalPreset("Daily", 1440)
)

private fun everyLabel(minutes: Long): String = when (minutes) {
    15L -> "Every 15 minutes"
    60L -> "Every hour"
    360L -> "Every 6 hours"
    1440L -> "Every day"
    else -> "Every $minutes minutes"
}

@Composable
fun ScheduledTasksScreen(viewModel: ScheduledTasksViewModel, onBack: (() -> Unit)? = null) {
    val tasks by viewModel.tasks.collectAsState()
    var showAddForm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Tasks",
            subtitle = "Runs automatically, even when the app is closed.",
            onBack = onBack,
            trailing = {
                if (showAddForm) {
                    AppButton("Cancel", onClick = { showAddForm = false }, kind = ButtonKind.Quiet, compact = true)
                } else {
                    AppButton("New task", onClick = { showAddForm = true }, compact = true, icon = Icons.Default.Add)
                }
            }
        )

        if (tasks.isEmpty() && !showAddForm) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Default.DateRange,
                    title = "No scheduled tasks",
                    body = "Repeat a command on a schedule, like a daily job search or a morning attendance check.",
                    actionLabel = "New task",
                    onAction = { showAddForm = true }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fadeEdges(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (showAddForm) {
                    item(key = "add-form") {
                        AddTaskForm(onCreate = { name, command, minutes ->
                            viewModel.create(name, command, minutes)
                            showAddForm = false
                        })
                    }
                }
                items(tasks, key = { it.id }) { task -> TaskCard(task, viewModel) }
            }
        }
    }
}

@Composable
private fun AddTaskForm(onCreate: (String, String, Long) -> Unit) {
    var name by remember { mutableStateOf("") }
    var command by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(presets.last()) }
    val c = AppTheme.colors

    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            AppTextField(name, { name = it }, label = "Name", singleLine = true)
            AppTextField(command, { command = it }, label = "Command, as you'd type it in Chat")
            Text("Repeat", style = MaterialTheme.typography.labelMedium, color = c.textMuted)
            SegmentedControl(
                options = presets, selected = selected,
                label = { it.label }, onSelect = { selected = it }
            )
            AppButton(
                "Create task",
                onClick = { onCreate(name.trim(), command.trim(), selected.minutes) },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && command.isNotBlank()
            )
        }
    }
}

@Composable
private fun TaskCard(task: ScheduledTaskEntity, viewModel: ScheduledTasksViewModel) {
    val c = AppTheme.colors
    var confirmDelete by remember { mutableStateOf(false) }
    val reduced = rememberReducedMotion()
    val sizeSpec: FiniteAnimationSpec<IntSize> =
        if (reduced) snap() else spring(dampingRatio = 1f, stiffness = 400f)

    AppCard(Modifier.fillMaxWidth().animateContentSize(animationSpec = sizeSpec)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(
                        task.name, style = MaterialTheme.typography.titleMedium,
                        color = if (task.enabled) c.text else c.textMuted
                    )
                    Text(
                        if (task.enabled) "Runs on schedule" else "Paused",
                        style = MaterialTheme.typography.bodySmall, color = c.textMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Switch(
                    checked = task.enabled,
                    onCheckedChange = { viewModel.setEnabled(task, it) },
                    colors = appSwitchColors()
                )
            }

            Box(
                Modifier
                    .padding(top = 12.dp)
                    .fillMaxWidth()
                    .clip(AppShapes.inset)
                    .background(c.raised)
                    .padding(12.dp)
            ) {
                Text(task.commandText, style = MaterialTheme.typography.bodyMedium, color = c.text)
            }

            Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                StatusPill(everyLabel(task.intervalMinutes))
                Spacer(Modifier.weight(1f))
                AppButton("Delete", onClick = { confirmDelete = true }, kind = ButtonKind.Destructive, compact = true)
            }

            val lastRun = task.lastRunAt
            if (lastRun != null || task.lastResultSummary != null) {
                Text(
                    buildString {
                        if (lastRun != null) {
                            append("Last run ")
                            append(DateUtils.getRelativeTimeSpanString(lastRun, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS))
                        }
                        task.lastResultSummary?.let {
                            if (isNotEmpty()) append(". ")
                            append(it)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall, color = c.textMuted,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this task?") },
            text = { Text("\"${task.name}\" will stop running. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(task); confirmDelete = false }) {
                    Text("Delete", color = c.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel", color = c.textMuted) }
            },
            containerColor = c.card,
            titleContentColor = c.text,
            textContentColor = c.textMuted
        )
    }
}
