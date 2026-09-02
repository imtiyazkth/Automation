package com.personalai.os.core.automation

import com.personalai.os.core.orchestrator.TaskStep

data class PendingApproval(
    val id: String,
    val step: TaskStep,
    val reason: String,
    val draftSummary: String
)

/**
 * Holds actions that PolicyEngine flagged as RequireApproval until the user
 * taps Send / Edit / Ignore in the Unified Inbox or Head Agent chat. This is
 * the queue backing the "[Send] [Edit] [Ignore] [Always use this response]"
 * buttons from blueprint Part 12.
 */
class ApprovalManager {
    private val pending = mutableMapOf<String, PendingApproval>()

    fun enqueue(approval: PendingApproval) { pending[approval.id] = approval }
    fun all(): List<PendingApproval> = pending.values.toList()
    fun resolve(id: String): PendingApproval? = pending.remove(id)
}
