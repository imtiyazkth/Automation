package com.personalai.os.ui.audit

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.personalai.os.core.security.AuditEntry
import com.personalai.os.ui.components.AppCard
import com.personalai.os.ui.components.EmptyState
import com.personalai.os.ui.components.IconAction
import com.personalai.os.ui.components.ScreenHeader
import com.personalai.os.ui.components.StatusPill
import com.personalai.os.ui.components.Tone
import com.personalai.os.ui.components.fadeEdges
import com.personalai.os.ui.components.humanize
import com.personalai.os.ui.components.rememberReducedMotion
import com.personalai.os.ui.components.toneColor
import com.personalai.os.ui.theme.AppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun toneFor(result: String): Tone = when (result) {
    "SUCCESS" -> Tone.Accent
    "FAILED", "BLOCKED" -> Tone.Danger
    else -> Tone.Warning
}

private fun resultLabel(result: String): String = when (result) {
    "SUCCESS" -> "Done"
    "PARTIAL_SUCCESS" -> "Partly done"
    "REQUIRES_USER_ACTION" -> "Needs you"
    "FAILED" -> "Failed"
    "BLOCKED" -> "Blocked"
    else -> result.humanize()
}

@Composable
fun AuditLogScreen(viewModel: AuditLogViewModel, onBack: (() -> Unit)? = null, onOpenChat: () -> Unit = {}) {
    val groups by viewModel.groups.collectAsState()

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            title = "Activity",
            subtitle = "Everything your agents did, newest first.",
            onBack = onBack,
            trailing = { IconAction(Icons.Default.Refresh, "Refresh", onClick = { viewModel.refresh() }) }
        )

        if (groups.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.AutoMirrored.Filled.List,
                    title = "No activity yet",
                    body = "Every action an agent takes will be recorded here, including the ones it was blocked from taking.",
                    actionLabel = "Give the assistant a task",
                    onAction = onOpenChat
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fadeEdges(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(groups, key = { it.startTimestamp }) { group -> TrajectoryCard(group) }
            }
        }
    }
}

@Composable
private fun TrajectoryCard(group: TrajectoryGroup) {
    val c = AppTheme.colors
    val reduced = rememberReducedMotion()
    var expanded by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("MMM d, HH:mm", Locale.getDefault()) }

    val sizeSpec: FiniteAnimationSpec<IntSize> =
        if (reduced) snap() else spring(dampingRatio = 1f, stiffness = 400f)
    val chevron by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = if (reduced) snap<Float>() else spring<Float>(dampingRatio = 1f, stiffness = 500f),
        label = "chevron"
    )

    val total = group.entries.size
    val ok = group.entries.count { it.result == "SUCCESS" }
    val problems = group.entries.count { it.result == "FAILED" || it.result == "BLOCKED" }
    val summary = buildString {
        append("$total step${if (total == 1) "" else "s"}")
        if (problems > 0) append(", $problems issue${if (problems == 1) "" else "s"}")
    }

    AppCard(Modifier.fillMaxWidth().animateContentSize(animationSpec = sizeSpec), onClick = { expanded = !expanded }) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(formatter.format(Date(group.startTimestamp)), style = MaterialTheme.typography.titleSmall, color = c.text)
                    Text(summary, style = MaterialTheme.typography.bodySmall, color = c.textMuted, modifier = Modifier.padding(top = 2.dp))
                }
                when {
                    problems > 0 -> StatusPill("Needs a look", tone = Tone.Danger)
                    ok == total -> StatusPill("Done", tone = Tone.Accent)
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = c.textMuted,
                    modifier = Modifier.rotate(chevron)
                )
            }

            if (expanded) {
                val steps = group.entries.sortedBy { it.timestamp }
                Column(Modifier.padding(top = 16.dp)) {
                    steps.forEachIndexed { i, entry -> TrajectoryStep(entry, isLast = i == steps.lastIndex) }
                }
            }
        }
    }
}

@Composable
private fun TrajectoryStep(entry: AuditEntry, isLast: Boolean) {
    val c = AppTheme.colors
    val tone = toneFor(entry.result)
    val color = toneColor(tone)
    val time = remember(entry.timestamp) { SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(entry.timestamp)) }

    Row(Modifier.height(IntrinsicSize.Min)) {
        Column(Modifier.width(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.padding(top = 5.dp).size(8.dp).background(color, CircleShape))
            if (!isLast) {
                Box(Modifier.padding(top = 4.dp).width(1.dp).weight(1f).background(c.hairline))
            }
        }
        Column(Modifier.weight(1f).padding(start = 12.dp, bottom = if (isLast) 0.dp else 16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(entry.action.humanize(), style = MaterialTheme.typography.titleSmall, color = c.text, modifier = Modifier.weight(1f))
                StatusPill(resultLabel(entry.result), tone = tone)
            }
            Text(
                "${entry.actor.humanize()} at $time",
                style = MaterialTheme.typography.bodySmall, color = c.textMuted,
                modifier = Modifier.padding(top = 2.dp)
            )
            entry.detail?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = c.text, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
