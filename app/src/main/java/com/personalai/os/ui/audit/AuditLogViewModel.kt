package com.personalai.os.ui.audit

import androidx.lifecycle.ViewModel
import com.personalai.os.core.security.AuditEntry
import com.personalai.os.core.security.AuditLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuditLogViewModel(private val auditLogger: AuditLogger) : ViewModel() {

    private val _entries = MutableStateFlow<List<AuditEntry>>(auditLogger.recent(200))
    val entries: StateFlow<List<AuditEntry>> = _entries

    fun refresh() {
        _entries.value = auditLogger.recent(200)
    }
}
