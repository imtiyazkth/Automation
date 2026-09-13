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

class HeadAgent(
    private val intentDetector: IntentDetector,
    private val taskPlanner: TaskPlanner,
    private val registry: AgentRegistry,
    private val policyEngine: PolicyEngine,
    private val modeStore: AutomationModeStore,
    private val approvalManager: ApprovalManager,
    private val auditLogger: AuditLogger
) {

    suspend fun handle(userInput: String): List<ExecutionReport> {
        val intent = intentDetector.detect(userInput)

        if (intent.intentType == "help") {
            val capabilities = registry.all().joinToString("\n") { "- ${it.name}: ${it.description}" }
            return listOf(ExecutionReport.Success("Here's what I can currently help with:\n$capabilities"))
        }

        val plan = taskPlanner.plan(intent)

        if (plan.steps.isEmpty()) {
            val message = if (intent.intentType != "unknown") {
                "I understood that as a '${intent.intentType}' request, but no agent is wired up to handle it yet."
            } else {
                "I'm not sure what you'd like me to do with: \"$userInput\""
            }
            return listOf(
                ExecutionReport.RequiresUserAction(
                    message = message,
                    reason = "No task plan for intent '${intent.intentType}' (confidence=${intent.confidence}, source=${intent.source})"
                )
            )
        }

        val reports = mutableListOf<ExecutionReport>()
        val completedStepIds = mutableSetOf<String>()

        for (step in plan.steps) {
            if (step.dependsOn.any { it !in completedStepIds }) {
                reports.add(ExecutionReport.PartialSuccess(
                    message = "Step '${step.action}' deferred - waiting on ${step.dependsOn}",
                    completed = completedStepIds.toList(),
                    remaining = listOf(step.id)
                ))
                continue
            }

            val agentDef = registry.definitionOf(step.agentId)
            if (agentDef == null) {
                reports.add(ExecutionReport.Failed("No registered agent for id '${step.agentId}'"))
                auditLogger.log(AuditEntry(System.currentTimeMillis(), "head-agent", step.action, step.agentId, "FAILED", "unknown agent"))
                continue
            }

            val mode = modeStore.modeFor(step.agentId)
            val decision = policyEngine.evaluate(agentDef, step.action, mode)

            when (decision) {
                is PolicyDecision.Deny -> {
                    reports.add(ExecutionReport.Failed("Blocked: ${decision.reason}"))
                    auditLogger.log(AuditEntry(System.currentTimeMillis(), "head-agent", step.action, step.agentId, "BLOCKED", decision.reason))
                }
                is PolicyDecision.RequireApproval -> {
                    val approvalId = UUID.randomUUID().toString()
                    approvalManager.enqueue(
                        PendingApproval(
                            id = approvalId,
                            step = step,
                            reason = decision.reason,
                            draftSummary = "Agent '${agentDef.name}' wants to run '${step.action}'"
                        )
                    )
                    reports.add(ExecutionReport.RequiresUserAction(
                        message = "Needs your approval: ${agentDef.name} -> ${step.action}. Check the Approvals tab.",
                        reason = decision.reason
                    ))
                    auditLogger.log(AuditEntry(System.currentTimeMillis(), "head-agent", step.action, step.agentId, "REQUIRES_USER_ACTION", decision.reason))
                }
                PolicyDecision.Allow -> {
                    val agentImpl = registry.implementationOf(step.agentId)
                    val result = if (agentImpl == null) {
                        ExecutionReport.Failed("Agent '${step.agentId}' has a definition but no registered implementation yet")
                    } else {
                        runCatching { agentImpl.execute(step) }
                            .getOrElse { ExecutionReport.Failed("Unhandled error in '${step.agentId}'", it) }
                    }
                    reports.add(result)
                    if (result is ExecutionReport.Success) completedStepIds.add(step.id)
                    auditLogger.log(AuditEntry(
                        System.currentTimeMillis(), step.agentId, step.action, agentDef.name,
                        resultLabel(result), result.message
                    ))
                }
            }
        }

        return reports
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

    private fun resultLabel(report: ExecutionReport): String = when (report) {
        is ExecutionReport.Success -> "SUCCESS"
        is ExecutionReport.PartialSuccess -> "PARTIAL_SUCCESS"
        is ExecutionReport.Failed -> "FAILED"
        is ExecutionReport.RequiresUserAction -> "REQUIRES_USER_ACTION"
    }
}
