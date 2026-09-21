package com.personalai.os.core.orchestrator

import com.personalai.os.core.agents.AgentRegistry
import com.personalai.os.core.automation.ApprovalManager
import com.personalai.os.core.automation.AutomationModeStore
import com.personalai.os.core.automation.PendingApproval
import com.personalai.os.core.security.AuditEntry
import com.personalai.os.core.security.AuditLogger
import com.personalai.os.core.security.PolicyDecision
import com.personalai.os.core.security.PolicyEngine
import java.util.UUID

data class PendingClarification(val step: TaskStep, val missingFields: List<String>, val attempts: Int = 0)

data class HandledResult(
    val reports: List<ExecutionReport>,
    val pendingClarification: PendingClarification? = null,
    val queuedApprovalId: String? = null
)

class HeadAgent(
    private val intentDetector: IntentDetector,
    private val taskPlanner: TaskPlanner,
    private val registry: AgentRegistry,
    private val policyEngine: PolicyEngine,
    private val modeStore: AutomationModeStore,
    private val approvalManager: ApprovalManager,
    private val auditLogger: AuditLogger
) {

    companion object {
        private const val MAX_CLARIFICATION_ATTEMPTS = 3
    }

    suspend fun handle(userInput: String): HandledResult {
        val intent = intentDetector.detect(userInput)

        if (intent.intentType == "help") {
            val capabilities = registry.all().joinToString("\n") { "- ${it.name}: ${it.description}" }
            return HandledResult(listOf(ExecutionReport.Success("Here's what I can currently help with:\n$capabilities")))
        }

        val plan = taskPlanner.plan(intent)

        if (plan.steps.isEmpty()) {
            val message = if (intent.intentType != "unknown") {
                "I understood that as a '${intent.intentType}' request, but no agent is wired up to handle it yet."
            } else {
                "I'm not sure what you'd like me to do with: \"$userInput\""
            }
            return HandledResult(listOf(ExecutionReport.RequiresUserAction(
                message = message,
                reason = "No task plan for intent '${intent.intentType}' (confidence=${intent.confidence}, source=${intent.source})"
            )))
        }

        val reports = mutableListOf<ExecutionReport>()
        val completedStepIds = mutableSetOf<String>()
        var lastPending: PendingClarification? = null
        var lastQueuedApprovalId: String? = null

        for (step in plan.steps) {
            if (step.dependsOn.any { it !in completedStepIds }) {
                reports.add(ExecutionReport.PartialSuccess(
                    message = "Step '${step.action}' deferred - waiting on ${step.dependsOn}",
                    completed = completedStepIds.toList(),
                    remaining = listOf(step.id)
                ))
                continue
            }

            val (result, queuedId) = runStep(step)
            reports.add(result)
            if (result is ExecutionReport.Success) completedStepIds.add(step.id)
            if (queuedId != null) lastQueuedApprovalId = queuedId

            if (plan.steps.size == 1 && result is ExecutionReport.RequiresUserAction) {
                val missing = parseMissingFields(result.reason)
                if (missing.isNotEmpty()) lastPending = PendingClarification(step, missing)
            }
        }

        return HandledResult(reports, lastPending, lastQueuedApprovalId)
    }

    suspend fun resolveClarification(pending: PendingClarification, additionalText: String): HandledResult {
        val attempts = pending.attempts + 1
        if (attempts > MAX_CLARIFICATION_ATTEMPTS) {
            return HandledResult(listOf(ExecutionReport.RequiresUserAction(
                message = "I'm having trouble understanding this one - let's start fresh. Try rephrasing the whole request in a single message.",
                reason = "clarification attempts exceeded"
            )))
        }

        val field = pending.missingFields.first()
        val cleanedValue = cleanSlotValue(field, additionalText)
        val updatedStep = pending.step.copy(params = pending.step.params + (field to cleanedValue))
        val stillMissing = pending.missingFields.drop(1)

        if (stillMissing.isNotEmpty()) {
            return HandledResult(
                reports = listOf(ExecutionReport.RequiresUserAction(
                    message = "Got it. And ${promptFor(stillMissing.first())}",
                    reason = "missing ${stillMissing.joinToString("/")}"
                )),
                pendingClarification = PendingClarification(updatedStep, stillMissing, attempts)
            )
        }

        val (result, queuedId) = runStep(updatedStep)
        val newPending = if (result is ExecutionReport.RequiresUserAction) {
            val missing = parseMissingFields(result.reason)
            if (missing.isNotEmpty()) PendingClarification(updatedStep, missing, attempts) else null
        } else null

        return HandledResult(listOf(result), newPending, queuedId)
    }

    suspend fun executeApproved(approval: PendingApproval): ExecutionReport {
        val step = approval.step
        val agentDef = registry.definitionOf(step.agentId)
            ?: return ExecutionReport.Failed("No registered agent for id '${step.agentId}'")
        val agentImpl = registry.implementationOf(step.agentId)
            ?: return ExecutionReport.Failed("Agent '${step.agentId}' has a definition but no registered implementation yet")

        val result = runCatching { agentImpl.execute(step) }
            .getOrElse { ExecutionReport.Failed("Unhandled error in '${step.agentId}'", it) }

        auditLogger.log(AuditEntry(
            System.currentTimeMillis(), step.agentId, step.action, agentDef.name,
            resultLabel(result), "approved by user - ${result.message}"
        ))
        return result
    }

    private suspend fun runStep(step: TaskStep): Pair<ExecutionReport, String?> {
        val agentDef = registry.definitionOf(step.agentId)
        if (agentDef == null) {
            auditLogger.log(AuditEntry(System.currentTimeMillis(), "head-agent", step.action, step.agentId, "FAILED", "unknown agent"))
            return ExecutionReport.Failed("No registered agent for id '${step.agentId}'") to null
        }

        val mode = modeStore.modeFor(step.agentId)
        val decision = policyEngine.evaluate(agentDef, step.action, mode)

        return when (decision) {
            is PolicyDecision.Deny -> {
                auditLogger.log(AuditEntry(System.currentTimeMillis(), "head-agent", step.action, step.agentId, "BLOCKED", decision.reason))
                ExecutionReport.Failed("Blocked: ${decision.reason}") to null
            }
            is PolicyDecision.RequireApproval -> {
                val approvalId = UUID.randomUUID().toString()
                approvalManager.enqueue(
                    PendingApproval(
                        id = approvalId, step = step, reason = decision.reason,
                        draftSummary = "Agent '${agentDef.name}' wants to run '${step.action}'"
                    )
                )
                auditLogger.log(AuditEntry(System.currentTimeMillis(), "head-agent", step.action, step.agentId, "REQUIRES_USER_ACTION", decision.reason))
                ExecutionReport.RequiresUserAction(
                    message = "Needs your approval before I do this.",
                    reason = decision.reason
                ) to approvalId
            }
            PolicyDecision.Allow -> {
                val agentImpl = registry.implementationOf(step.agentId)
                val result = if (agentImpl == null) {
                    ExecutionReport.Failed("Agent '${step.agentId}' has a definition but no registered implementation yet")
                } else {
                    runCatching { agentImpl.execute(step) }
                        .getOrElse { ExecutionReport.Failed("Unhandled error in '${step.agentId}'", it) }
                }
                auditLogger.log(AuditEntry(
                    System.currentTimeMillis(), step.agentId, step.action, agentDef.name,
                    resultLabel(result), result.message
                ))
                result to null
            }
        }
    }

    private fun parseMissingFields(reason: String): List<String> =
        if (reason.startsWith("missing ")) reason.removePrefix("missing ").split("/").map { it.trim() }.filter { it.isNotBlank() }
        else emptyList()

    private fun cleanSlotValue(field: String, raw: String): String = when (field) {
        "recipient" -> raw.trim()
            .removePrefix("to ").removePrefix("To ")
            .removePrefix("for ").removePrefix("For ")
            .trim()
        else -> raw.trim()
    }

    private fun promptFor(field: String): String = when (field) {
        "recipient" -> "who should I send it to?"
        "message" -> "what should it say?"
        "url" -> "which link should I check?"
        "job_description" -> "paste the job description you want evaluated"
        "query" -> "what should I search for?"
        else -> "what's the $field?"
    }

    private fun resultLabel(report: ExecutionReport): String = when (report) {
        is ExecutionReport.Success -> "SUCCESS"
        is ExecutionReport.PartialSuccess -> "PARTIAL_SUCCESS"
        is ExecutionReport.Failed -> "FAILED"
        is ExecutionReport.RequiresUserAction -> "REQUIRES_USER_ACTION"
    }
}
