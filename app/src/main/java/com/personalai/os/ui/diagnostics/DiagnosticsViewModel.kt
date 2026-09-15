package com.personalai.os.ui.diagnostics

import androidx.lifecycle.ViewModel
import com.personalai.os.util.CrashLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class DiagnosticsUiState(
    val logFileNames: List<String> = emptyList(),
    val selectedContent: String? = null
)

class DiagnosticsViewModel : ViewModel() {

    private val _state = MutableStateFlow(DiagnosticsUiState())
    val state: StateFlow<DiagnosticsUiState> = _state

    init { refresh() }

    fun refresh() {
        val files = CrashLogger.getRecentLogs()
        _state.value = _state.value.copy(logFileNames = files.map { it.name })
    }

    fun open(fileName: String) {
        val file = CrashLogger.getRecentLogs().firstOrNull { it.name == fileName }
        _state.value = _state.value.copy(selectedContent = file?.readText() ?: "Couldn't read $fileName")
    }

    fun closeDetail() {
        _state.value = _state.value.copy(selectedContent = null)
    }

    fun clearAll() {
        CrashLogger.clearLogs()
        refresh()
    }
}
