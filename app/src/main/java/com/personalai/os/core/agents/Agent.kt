package com.personalai.os.core.agents

import com.personalai.os.core.orchestrator.ExecutionReport
import com.personalai.os.core.orchestrator.TaskStep

/**
 * Executable behavior for one specialized agent. Implementations must stay
 * strictly within the tools/permissions declared in their [AgentDefinition]
 * - the Head Agent's PolicyEngine enforces this from the outside, but
 * well-behaved agents should never even attempt otherwise (defense in depth).
 */
interface Agent {
    val definitionId: String
    suspend fun execute(step: TaskStep): ExecutionReport
}
