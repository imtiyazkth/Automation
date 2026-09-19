package com.personalai.os.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalai.os.core.orchestrator.ExecutionReport
import com.personalai.os.core.orchestrator.HeadAgent
import com.personalai.os.core.orchestrator.PendingClarification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatMessage(val fromUser: Boolean, val text: String)

class ChatViewModel(private val headAgent: HeadAgent) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private var pending: PendingClarification? = null

    fun send(text: String) {
        _messages.update { it + ChatMessage(fromUser = true, text = text) }
        viewModelScope.launch {
            val currentPending = pending
            val result = if (currentPending != null) {
                headAgent.resolveClarification(currentPending, text)
            } else {
                headAgent.handle(text)
            }
            pending = result.pendingClarification

            val reply = result.reports.joinToString("\n") { describe(it) }
            _messages.update { it + ChatMessage(fromUser = false, text = reply.ifBlank { "(no response)" }) }
        }
    }

    private fun describe(report: ExecutionReport): String = when (report) {
        is ExecutionReport.Success -> report.message
        is ExecutionReport.PartialSuccess -> "${report.message} (remaining: ${report.remaining.joinToString()})"
        is ExecutionReport.Failed -> "Couldn't do that: ${report.message}"
        is ExecutionReport.RequiresUserAction -> report.message
    }
}
