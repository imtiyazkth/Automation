package com.personalai.os.core.workflows

import com.personalai.os.core.agents.AgentRegistry
import com.personalai.os.core.automation.AutomationModeStore
import com.personalai.os.core.orchestrator.ExecutionReport
import com.personalai.os.core.orchestrator.TaskStep
import com.personalai.os.core.security.PolicyEngine

/**
 * Fires rules whose trigger matches an incoming event and whose conditions
 * all evaluate true, then routes each Action through the SAME
 * PolicyEngine/AgentRegistry path the Head Agent uses for direct commands -
 * a workflow-triggered action gets no special trust.
 */
class WorkflowEngine(
    private val registry: AgentRegistry,
    private val policyEngine: PolicyEngine,
    private val modeStore: AutomationModeStore,
    private val conditionEvaluator: ConditionEvaluator = ConditionEvaluator()
) {
    private val workflows = mutableListOf<Workflow>()

    fun register(workflow: Workflow) { workflows.add(workflow) }

    suspend fun onEvent(eventType: String, context: Map<String, String>): List<ExecutionReport> {
        val reports = mutableListOf<ExecutionReport>()

        for (workflow in workflows.filter { it.enabled }) {
            for (rule in workflow.rules) {
                if (rule.trigger.type != eventType) continue
                val conditionsMet = rule.conditions.all { conditionEvaluator.evaluate(it, context) }
                if (!conditionsMet) continue

                for (action in rule.actions) {
                    val agentDef = registry.definitionOf(action.agentId) ?: continue
                    val mode = modeStore.modeFor(action.agentId)
                    val decision = policyEngine.evaluate(agentDef, action.action, mode)
                    if (decision !is com.personalai.os.core.security.PolicyDecision.Allow) {
                        reports.add(ExecutionReport.RequiresUserAction(
                            "Workflow '${workflow.name}' rule '${rule.name}' needs approval",
                            decision.toString()
                        ))
                        continue
                    }
                    val impl = registry.implementationOf(action.agentId) ?: continue
                    val result = runCatching {
                        impl.execute(TaskStep(id = rule.id, agentId = action.agentId, action = action.action, params = action.params))
                    }.getOrElse { ExecutionReport.Failed("Workflow action failed", it) }
                    reports.add(result)
                }
            }
        }
        return reports
    }
}
