package com.personalai.os.ui.agentcenter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personalai.os.core.automation.AutomationMode

@Composable
fun AgentCenterScreen(viewModel: AgentCenterViewModel) {
    val agents by viewModel.agents.collectAsState()

    LazyColumn(Modifier.fillMaxWidth().padding(12.dp)) {
        items(agents) { agent ->
            Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                Column(Modifier.padding(12.dp)) {
                    Text(agent.name, style = MaterialTheme.typography.titleMedium)
                    Text(agent.description, style = MaterialTheme.typography.bodySmall)
                    Text("Category: ${agent.category}  |  Risk: ${agent.risk}", style = MaterialTheme.typography.labelSmall)

                    Text(
                        if (agent.allPermissionsGranted) "Permissions: all granted"
                        else "Missing permissions: ${agent.missingPermissions.joinToString()}",
                        style = MaterialTheme.typography.labelSmall
                    )

                    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        AutomationMode.values().forEach { mode ->
                            val selected = agent.mode == mode
                            OutlinedButton(onClick = { viewModel.setMode(agent.id, mode) }) {
                                Text(if (selected) "[${mode.name}]" else mode.name)
                            }
                        }
                    }
                }
            }
        }
    }
}
