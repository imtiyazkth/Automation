package com.personalai.os.ui.audit

import androidx.lifecycle.ViewModel
import com.personalai.os.core.security.AuditEntry
import com.personalai.os.core.security.AuditLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class TrajectoryGroup(
    val startTimestamp: Long,
    val entries: List<AuditEntry>
)

class AuditLogViewModel(private val auditLogger: AuditLogger) : ViewModel() {

    private val _groups = MutableStateFlow<List<TrajectoryGroup>>(group(auditLogger.recent(200)))
    val groups: StateFlow<List<TrajectoryGroup>> = _groups

    fun refresh() {
        _groups.value = group(auditLogger.recent(200))
    }

    private fun group(entries: List<AuditEntry>): List<TrajectoryGroup> {
        if (entries.isEmpty()) return emptyList()
        val ascending = entries.sortedBy { it.timestamp }
        val clustered = mutableListOf<MutableList<AuditEntry>>()
        for (entry in ascending) {
            val lastGroup = clustered.lastOrNull()
            if (lastGroup != null && entry.timestamp - lastGroup.last().timestamp <= GROUP_WINDOW_MS) {
                lastGroup.add(entry)
            } else {
                clustered.add(mutableListOf(entry))
            }
        }
        return clustered.map { TrajectoryGroup(it.first().timestamp, it.toList()) }
            .sortedByDescending { it.startTimestamp }
    }

    companion object {
        private const val GROUP_WINDOW_MS = 3000L
    }
}
