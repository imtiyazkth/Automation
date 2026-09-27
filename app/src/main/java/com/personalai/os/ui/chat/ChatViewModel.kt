package com.personalai.os.ui.chat

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalai.os.core.ai.GeminiLiveClient
import com.personalai.os.core.ai.GeminiLiveEvent
import com.personalai.os.core.ai.LiveAudioCapture
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

enum class LiveVoiceState { IDLE, CONNECTING, LISTENING }

class ChatViewModel(
    private val headAgent: HeadAgent,
    private val approvalManager: ApprovalManager,
    appContext: Context
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private var pending: PendingClarification? = null

    private val geminiLiveClient = GeminiLiveClient()
    private val liveAudioCapture = LiveAudioCapture(appContext)

    private val _liveTranscript = MutableStateFlow("")
    val liveTranscript: StateFlow<String> = _liveTranscript

    private val _liveState = MutableStateFlow(LiveVoiceState.IDLE)
    val liveState: StateFlow<LiveVoiceState> = _liveState

    fun hasMicPermission(): Boolean = liveAudioCapture.hasPermission()

    fun startLiveVoice() {
        if (!geminiLiveClient.isConfigured()) {
            _messages.update { it + ChatMessage.Text(false, "Live voice needs GEMINI_API_KEY configured in local.properties.") }
            return
        }
        if (!liveAudioCapture.hasPermission()) {
            _messages.update { it + ChatMessage.Text(false, "Microphone permission is needed for live voice.") }
            return
        }
        _liveTranscript.value = ""
        _liveState.value = LiveVoiceState.CONNECTING

        viewModelScope.launch {
            geminiLiveClient.events.collect { event ->
                when (event) {
                    is GeminiLiveEvent.SetupComplete -> {
                        _liveState.value = LiveVoiceState.LISTENING
                        liveAudioCapture.start(viewModelScope) { chunk -> geminiLiveClient.sendAudioChunk(chunk) }
                    }
                    is GeminiLiveEvent.TextDelta -> _liveTranscript.update { it + event.text }
                    is GeminiLiveEvent.TurnComplete -> { }
                    is GeminiLiveEvent.Error -> {
                        _liveState.value = LiveVoiceState.IDLE
                        _messages.update { it + ChatMessage.Text(false, "Live voice error: ${event.message}") }
                    }
                    is GeminiLiveEvent.Closed -> _liveState.value = LiveVoiceState.IDLE
                }
            }
        }
        geminiLiveClient.connect()
    }

    fun stopLiveVoice() {
        liveAudioCapture.stop()
        geminiLiveClient.close()
        _liveState.value = LiveVoiceState.IDLE
        val transcript = _liveTranscript.value.trim()
        _liveTranscript.value = ""
        if (transcript.isNotBlank()) {
            send(transcript)
        }
    }

    override fun onCleared() {
        super.onCleared()
        liveAudioCapture.stop()
        geminiLiveClient.close()
    }

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
