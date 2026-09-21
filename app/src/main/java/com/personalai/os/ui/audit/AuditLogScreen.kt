package com.personalai.os.ui.audit

import android.provider.Settings
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.personalai.os.core.security.AuditEntry
import com.personalai.os.ui.theme.OsAccent
import com.personalai.os.ui.theme.OsDanger
import com.personalai.os.ui.theme.OsOnSurfaceMuted
import com.personalai.os.ui.theme.OsWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AuditLogScreen(viewModel: AuditLogViewModel) {
    val groups by viewModel.groups.collectAsState()
    val reducedMotion = isReducedMotionEnabled()

    Column(Modifier.fillMaxWidth().padding(12.dp)) {
        Button(onClick = { viewModel.refresh() }, modifier = Modifier.padding(bottom = 8.dp)) {
            Text("Refresh")
        }

        if (groups.isEmpty()) {
            Text("No activity yet.", color = OsOnSurfaceMuted)
        }

        LazyColumn {
            items(groups) { group -> TrajectoryCard(group, reducedMotion) }
        }
    }
}

@Composable
private fun isReducedMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

@Composable
private fun TrajectoryCard(group: TrajectoryGroup, reducedMotion: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val formatter = remember { SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault()) }

    val expandSpec: FiniteAnimationSpec<IntSize> = if (reducedMotion) {
        spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
    } else {
        spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow)
    }

    val successCount = group.entries.count { it.result == "SUCCESS" }
    val problemCount = group.entries.count { it.result == "FAILED" || it.result == "BLOCKED" }
    val summary = buildString {
        append("${group.entries.size} step${if (group.entries.size == 1) "" else "s"}")
        if (successCount > 0) append(" \u00b7 $successCount ok")
        if (problemCount > 0) append(" \u00b7 $problemCount issue${if (problemCount == 1) "" else "s"}")
    }

    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize(animationSpec = expandSpec)
            .clickable { expanded = !expanded }
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)) {
                    Text(
                        formatter.format(Date(group.startTimestamp)),
                        style = MaterialTheme.typography.titleSmall.copy(letterSpacing = (-0.2).sp),
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(summary, style = MaterialTheme.typography.bodySmall, color = OsOnSurfaceMuted)
                }
            }

            if (expanded) {
                Column(Modifier.padding(top = 8.dp)) {
                    group.entries.sortedBy { it.timestamp }.forEach { entry -> TrajectoryStep(entry) }
                }
            }
        }
    }
}

@Composable
private fun TrajectoryStep(entry: AuditEntry) {
    val color = when (entry.result) {
        "SUCCESS" -> OsAccent
        "FAILED", "BLOCKED" -> OsDanger
        else -> OsWarning
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text("\u25CF", color = color, modifier = Modifier.padding(end = 8.dp))
        Column {
            Text("${entry.actor} \u2192 ${entry.action}", style = MaterialTheme.typography.bodyMedium)
            entry.detail?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = OsOnSurfaceMuted) }
            Text(entry.result, style = MaterialTheme.typography.labelSmall, color = color)
        }
    }
}
