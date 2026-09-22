package com.personalai.os.ui.scheduled

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personalai.os.data.entities.ScheduledTaskEntity

private data class IntervalPreset(val label: String, val minutes: Long)
private val presets = listOf(
    IntervalPreset("15 min", 15),
    IntervalPreset("1 hour", 60),
    IntervalPreset("6 hours", 360),
    IntervalPreset("Daily", 1440)
)

@Composable
fun ScheduledTasksScreen(viewModel: ScheduledTasksViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    var showAddForm by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Runs automatically, even when the app is closed.", style = MaterialTheme.typography.bodySmall)
        }
        Button(onClick = { showAddForm = !showAddForm }, modifier = Modifier.padding(vertical = 8.dp)) {
            Text(if (showAddForm) "Cancel" else "+ New scheduled task")
        }

        if (showAddForm) {
            AddTaskForm(onCreate = { name, command, minutes ->
                viewModel.create(name, command, minutes)
                showAddForm = false
            })
        }

        if (tasks.isEmpty() && !showAddForm) {
            Text("No scheduled tasks yet.")
        }

        LazyColumn {
            items(tasks) { task -> TaskCard(task, viewModel) }
        }
    }
}

@Composable
private fun AddTaskForm(onCreate: (String, String, Long) -> Unit) {
    var name by remember { mutableStateOf("") }
    var command by remember { mutableStateOf("") }
    var selectedMinutes by remember { mutableStateOf(1440L) }

    Card(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
        Column(Modifier.padding(12.dp)) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                label = { Text("Name") }
            )
            OutlinedTextField(
                value = command, onValueChange = { command = it },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                label = { Text("Command (exactly as you'd type it in Chat)") }
            )
            Text("Repeat every:", style = MaterialTheme.typography.bodySmall)
            Row(Modifier.padding(top = 4.dp, bottom = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                presets.forEach { preset ->
                    OutlinedButton(onClick = { selectedMinutes = preset.minutes }) {
                        Text(if (selectedMinutes == preset.minutes) "[${preset.label}]" else preset.label)
                    }
                }
            }
            Button(
                onClick = { if (name.isNotBlank() && command.isNotBlank()) onCreate(name, command, selectedMinutes) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Create") }
        }
    }
}

@Composable
private fun TaskCard(task: ScheduledTaskEntity, viewModel: ScheduledTasksViewModel) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(task.name, style = MaterialTheme.typography.titleSmall)
                    Text("\"${task.commandText}\"", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Every ${presets.firstOrNull { it.minutes == task.intervalMinutes }?.label ?: "${task.intervalMinutes} min"}",
                        style = MaterialTheme.typography.labelSmall
                    )
                    task.lastResultSummary?.let {
                        Text("Last run: $it", style = MaterialTheme.typography.labelSmall)
                    }
                }
                Switch(checked = task.enabled, onCheckedChange = { viewModel.setEnabled(task, it) })
            }
            OutlinedButton(onClick = { viewModel.delete(task) }, modifier = Modifier.padding(top = 6.dp)) {
                Text("Delete")
            }
        }
    }
}
