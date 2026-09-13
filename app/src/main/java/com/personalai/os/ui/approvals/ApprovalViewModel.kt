package com.personalai.os.ui.approvals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.personalai.os.core.automation.ApprovalManager
import com.personalai.os.core.automation.PendingApproval
import com.personalai.os.core.orchestrator.ExecutionReport
import com.personalai.os.core.orchestrator.HeadAgent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ApprovalUiState(
    val pending: List<PendingApproval> = emptyList(),
    val lastResult: String? = null
)

class ApprovalViewModel(
    private val approvalManager: ApprovalManager,
    private val headAgent: HeadAgent
) : ViewModel() {

    private val _state = MutableStateFlow(ApprovalUiState(pending = approvalManager.all()))
    val state: StateFlow<ApprovalUiState> = _state

    fun refresh() {
        _state.value = _state.value.copy(pending = approvalManager.all())
    }

    fun send(id: String) {
        val approval = approvalManager.resolve(id) ?: return
        runExecution(approval)
    }

    fun sendEdited(id: String, newMessage: String) {
        val approval = approvalManager.resolve(id) ?: return
        val editedStep = approval.step.copy(params = approval.step.params + ("message" to newMessage))
        runExecution(approval.copy(step = editedStep))
    }

    fun ignore(id: String) {
        approvalManager.resolve(id)
        refresh()
    }

    private fun runExecution(approval: PendingApproval) {
        viewModelScope.launch {
            val result = headAgent.executeApproved(approval)
            _state.value = ApprovalUiState(
                pending = approvalManager.all(),
                lastResult = describe(result)
            )
        }
    }

    private fun describe(report: ExecutionReport): String = when (report) {
        is ExecutionReport.Success -> report.message
        is ExecutionReport.PartialSuccess -> report.message
        is ExecutionReport.Failed -> "Couldn't do that: ${report.message}"
        is ExecutionReport.RequiresUserAction -> "${report.message} - ${report.reason}"
    }
}
