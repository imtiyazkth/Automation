package com.personalai.os.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.personalai.os.ui.components.AppCard
import com.personalai.os.ui.components.IconAction
import com.personalai.os.ui.components.ScreenHeader
import com.personalai.os.ui.components.SkeletonBlock
import com.personalai.os.ui.components.StatusPill
import com.personalai.os.ui.components.Tone
import com.personalai.os.ui.components.fadeEdges
import com.personalai.os.ui.components.humanize
import com.personalai.os.ui.theme.AppTheme

private data class Stat(val label: String, val value: Int, val warn: Boolean = false)

@Composable
fun DashboardScreen(viewModel: DashboardViewModel, onOpenApprovals: () -> Unit = {}) {
    val state by viewModel.state.collectAsState()
    val c = AppTheme.colors

    val stats = listOf(
        Stat("Messages today", state.messagesToday),
        Stat("Active leads", state.activeLeads),
        Stat("Orders today", state.ordersToday),
        Stat("Alerts, last 24 hours", state.alertsToday, warn = state.alertsToday > 0)
    )

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Today",
            trailing = { IconAction(Icons.Default.Refresh, "Refresh", onClick = { viewModel.refresh() }) }
        )

        Column(Modifier.weight(1f).fadeEdges().verticalScroll(rememberScrollState())) {
            Column(
                Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusPill("Online", tone = Tone.Accent, showDot = true)
                    StatusPill("${state.automationMode.humanize()} mode")
                }

                if (state.loading) {
                    SkeletonBlock(Modifier.fillMaxWidth().height(92.dp))
                    repeat(2) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SkeletonBlock(Modifier.weight(1f).height(88.dp))
                            SkeletonBlock(Modifier.weight(1f).height(88.dp))
                        }
                    }
                } else {
                    if (state.needsApproval > 0) {
                        AppCard(Modifier.fillMaxWidth(), onClick = onOpenApprovals) {
                            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(state.needsApproval.toString(), style = MaterialTheme.typography.headlineMedium, color = c.accent)
                                Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("Waiting for your approval", style = MaterialTheme.typography.titleSmall, color = c.text)
                                    Text("Review before anything is sent.", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                                }
                                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = c.textMuted)
                            }
                        }
                    } else {
                        AppCard(Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = c.accent)
                                Spacer(Modifier.width(16.dp))
                                Column {
                                    Text("You're all caught up", style = MaterialTheme.typography.titleSmall, color = c.text)
                                    Text("Nothing is waiting for your approval.", style = MaterialTheme.typography.bodySmall, color = c.textMuted)
                                }
                            }
                        }
                    }

                    stats.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            pair.forEach { stat ->
                                AppCard(Modifier.weight(1f)) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(
                                            stat.value.toString(),
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = if (stat.warn) c.warning else c.text
                                        )
                                        Text(stat.label, style = MaterialTheme.typography.bodySmall, color = c.textMuted, modifier = Modifier.padding(top = 2.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
