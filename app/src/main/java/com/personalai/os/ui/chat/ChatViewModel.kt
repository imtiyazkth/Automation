package com.personalai.os.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalai.os.core.automation.ApprovalManager
import com.personalai.os.core.orchestrator.ExecutionReport
import com.personalai.os.core.orchestrator.HeadAgent
import com.personalai.os.core.orchestrator.PendingClarification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class ChatMessage {
    data class Text(val fromUser: Boolean, val text: String) : ChatMessage()
    data class ApprovalCard(
        val approvalId: String,
        val summary: String,
        val reason: String,
        val editableMessage: String?
    ) : ChatMessage()
}

class ChatViewModel(
    private val headAgent: HeadAgent,
    private val approvalManager: ApprovalManager
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private var pending: PendingClarification? = null

    fun send(text: String) {
        _messages.update { it + ChatMessage.Text(fromUser = true, text = text) }
        viewModelScope.launch {
            val currentPending = pending
            val result = if (currentPending != null) {
                headAgent.resolveClarification(currentPending, text)
            } else {
                headAgent.handle(text)
            }
            pending = result.pendingClarification

            val reply = result.reports.joinToString("\n") { describe(it) }
            _messages.update { it + ChatMessage.Text(fromUser = false, text = reply.ifBlank { "(no response)" }) }

            result.queuedApprovalId?.let { id ->
                val approval = approvalManager.get(id)
                if (approval != null) {
                    _messages.update {
                        it + ChatMessage.ApprovalCard(
                            approvalId = id,
                            summary = approval.draftSummary,
                            reason = approval.reason,
                            editableMessage = approval.step.params["message"] as? String
                        )
                    }
                }
            }
        }
    }

    fun approveSend(approvalId: String) {
        viewModelScope.launch {
            val approval = approvalManager.resolve(approvalId) ?: return@launch
            val result = headAgent.executeApproved(approval)
            replaceCardWithResult(approvalId, result)
        }
    }

    fun approveEdited(approvalId: String, newMessage: String) {
        viewModelScope.launch {
            val approval = approvalManager.resolve(approvalId) ?: return@launch
            val editedStep = approval.step.copy(params = approval.step.params + ("message" to newMessage))
            val result = headAgent.executeApproved(approval.copy(step = editedStep))
            replaceCardWithResult(approvalId, result)
        }
    }

    fun ignoreApproval(approvalId: String) {
        approvalManager.resolve(approvalId)
        _messages.update { list ->
            list.map { msg ->
                if (msg is ChatMessage.ApprovalCard && msg.approvalId == approvalId) {
                    ChatMessage.Text(fromUser = false, text = "Okay, ignored.")
                } else msg
            }
        }
    }

    private fun replaceCardWithResult(approvalId: String, result: ExecutionReport) {
        _messages.update { list ->
            list.map { msg ->
                if (msg is ChatMessage.ApprovalCard && msg.approvalId == approvalId) {
                    ChatMessage.Text(fromUser = false, text = describe(result))
                } else msg
            }
        }
    }

    private fun describe(report: ExecutionReport): String = when (report) {
        is ExecutionReport.Success -> report.message
        is ExecutionReport.PartialSuccess -> "${report.message} (remaining: ${report.remaining.joinToString()})"
        is ExecutionReport.Failed -> "Couldn't do that: ${report.message}"
        is ExecutionReport.RequiresUserAction -> report.message
    }
}
