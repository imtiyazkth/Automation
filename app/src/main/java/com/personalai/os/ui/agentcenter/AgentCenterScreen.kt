package com.personalai.os.ui.agentcenter

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.personalai.os.core.automation.AutomationMode
import com.personalai.os.ui.components.AppCard
import com.personalai.os.ui.components.ScreenHeader
import com.personalai.os.ui.components.SegmentedControl
import com.personalai.os.ui.components.StatusPill
import com.personalai.os.ui.components.Tone
import com.personalai.os.ui.components.fadeEdges
import com.personalai.os.ui.components.humanize
import com.personalai.os.ui.components.permissionLabel
import com.personalai.os.ui.theme.AppShapes
import com.personalai.os.ui.theme.AppTheme

@Composable
fun AgentCenterScreen(viewModel: AgentCenterViewModel) {
    val agents by viewModel.agents.collectAsState()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Agents",
            subtitle = "${agents.size} available. Choose how much each one can do on its own."
        )
        LazyColumn(
            modifier = Modifier.weight(1f).fadeEdges(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(agents, key = { it.id }) { agent -> AgentCard(agent, viewModel) }
        }
    }
}

@Composable
private fun AgentCard(agent: AgentUiModel, viewModel: AgentCenterViewModel) {
    val c = AppTheme.colors
    AppCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier.size(40.dp).clip(AppShapes.control).background(c.raised),
                    contentAlignment = Alignment.Center
                ) {
                    Text(agent.name.take(1).uppercase(), style = MaterialTheme.typography.titleMedium, color = c.text)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(agent.name, style = MaterialTheme.typography.titleMedium, color = c.text)
                    Text(
                        agent.description, style = MaterialTheme.typography.bodySmall, color = c.textMuted,
                        maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusPill(agent.category.humanize())
                StatusPill(
                    "${agent.risk.humanize()} risk",
                    tone = when (agent.risk.lowercase()) {
                        "medium" -> Tone.Warning
                        "high", "critical" -> Tone.Danger
                        else -> Tone.Neutral
                    }
                )
                if (agent.allPermissionsGranted) {
                    StatusPill("Ready", tone = Tone.Accent, showDot = true)
                } else {
                    val n = agent.missingPermissions.size
                    StatusPill("$n permission${if (n == 1) "" else "s"} missing", tone = Tone.Warning)
                }
            }

            if (!agent.allPermissionsGranted) {
                Text(
                    "Needs: ${agent.missingPermissions.joinToString { permissionLabel(it) }}",
                    style = MaterialTheme.typography.bodySmall, color = c.textMuted,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            SegmentedControl(
                options = AutomationMode.values().toList(),
                selected = agent.mode,
                label = { it.name.humanize() },
                onSelect = { viewModel.setMode(agent.id, it) },
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(modeHint(agent.mode), style = MaterialTheme.typography.bodySmall, color = c.textMuted, modifier = Modifier.padding(top = 8.dp))
        }
    }
}

private fun modeHint(mode: AutomationMode): String = when (mode) {
    AutomationMode.MANUAL -> "Every action waits for your approval."
    AutomationMode.SMART -> "Routine actions run on their own. Risky ones ask first."
    AutomationMode.FULL -> "Runs on its own. Critical actions still ask."
}
