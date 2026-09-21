package com.personalai.os.core.automation

import com.personalai.os.core.orchestrator.TaskStep

data class PendingApproval(
    val id: String,
    val step: TaskStep,
    val reason: String,
    val draftSummary: String
)

class ApprovalManager {
    private val pending = mutableMapOf<String, PendingApproval>()

    fun enqueue(approval: PendingApproval) { pending[approval.id] = approval }
    fun all(): List<PendingApproval> = pending.values.toList()
    fun get(id: String): PendingApproval? = pending[id]
    fun resolve(id: String): PendingApproval? = pending.remove(id)
}
