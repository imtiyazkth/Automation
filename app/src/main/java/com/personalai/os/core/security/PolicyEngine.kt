package com.personalai.os.core.security

import com.personalai.os.core.agents.AgentDefinition
import com.personalai.os.core.automation.AutomationMode

/** Result of a policy check - the Head Agent must not proceed past a Deny without approval. */
sealed class PolicyDecision {
    object Allow : PolicyDecision()
    data class RequireApproval(val reason: String) : PolicyDecision()
    data class Deny(val reason: String) : PolicyDecision()
}

/**
 * The single choke point every action must pass through before execution.
 * Combines: does the agent declare this permission, has the user granted
 * it, what automation mode is active, and does this specific action match
 * one of the agent's own requires_approval_for triggers.
 */
class PolicyEngine(private val permissionManager: PermissionManager) {

    fun evaluate(
        agentDef: AgentDefinition,
        action: String,
        mode: AutomationMode,
        isNewOrUnknownRecipient: Boolean = false
    ): PolicyDecision {
        // 1. Every permission the agent needs for this call must be granted.
        val missing = agentDef.permissions.filterNot { permissionManager.isGranted(it) }
        if (missing.isNotEmpty()) {
            return PolicyDecision.Deny("Missing permission(s): ${missing.joinToString()}")
        }

        // 2. Actions in the agent's own approval list ALWAYS require approval,
        //    regardless of automation mode - this is what lets an agent.json
        //    author hard-code "never fully automate this" rules.
        if (action in agentDef.requires_approval_for) {
            return PolicyDecision.RequireApproval("'$action' is flagged as always-review for ${agentDef.name}")
        }

        // 3. Unknown/new recipients are always a review trigger for any
        //    communication-capable agent, independent of mode.
        if (isNewOrUnknownRecipient) {
            return PolicyDecision.RequireApproval("Recipient has not been messaged before")
        }

        // 4. Automation mode gates everything else.
        return when (mode) {
            AutomationMode.MANUAL -> PolicyDecision.RequireApproval("Manual mode - every action needs confirmation")
            AutomationMode.SMART -> if (agentDef.risk in listOf("high", "critical"))
                PolicyDecision.RequireApproval("High-risk action under Smart mode")
            else PolicyDecision.Allow
            AutomationMode.FULL -> if (agentDef.risk == "critical")
                PolicyDecision.RequireApproval("Critical-risk actions are never fully automated")
            else PolicyDecision.Allow
        }
    }
}
