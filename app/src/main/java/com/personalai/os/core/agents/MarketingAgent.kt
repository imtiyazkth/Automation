package com.personalai.os.core.agents

import com.personalai.os.core.automation.AutomationMode
import com.personalai.os.core.automation.AutomationModeStore
import com.personalai.os.core.orchestrator.ExecutionReport
import com.personalai.os.core.orchestrator.TaskStep

/**
 * Deliberately scoped to business intents ONLY (order status, product FAQs,
 * store hours, "how to order") - never open-domain conversation. This is
 * what keeps a WhatsApp Business Cloud API integration compliant with
 * Meta's 2026 rule against general-purpose AI chatbots (blueprint Part 13).
 */
class MarketingAgent(private val modeStore: AutomationModeStore) : Agent {
    override val definitionId = "marketing-agent"

    private val allowedIntents = setOf("order_status", "product_faq", "store_hours", "how_to_order", "escalate")

    override suspend fun execute(step: TaskStep): ExecutionReport {
        return when (step.action) {
            "toggle_mode" -> {
                val turnOn = (step.params["state"] as? String)?.contains("on", ignoreCase = true) ?: true
                modeStore.setModeFor(definitionId, if (turnOn) AutomationMode.SMART else AutomationMode.MANUAL)
                ExecutionReport.Success("Marketing Mode is now ${if (turnOn) "ON" else "OFF"}")
            }
            "classify_and_reply" -> {
                val classified = classify(step.params["message"] as? String ?: "")
                if (classified !in allowedIntents) {
                    return ExecutionReport.RequiresUserAction(
                        "This message is outside the approved business scope - needs a human reply.",
                        "off_topic_or_unclassified"
                    )
                }
                ExecutionReport.Success("Drafted a scoped reply for intent '$classified'", mapOf("intent" to classified))
            }
            "crm_query" -> ExecutionReport.Success("Today's orders: (wire to CrmDao)")
            else -> ExecutionReport.Failed("MarketingAgent has no handler for action '${step.action}'")
        }
    }

    /** Placeholder classifier - route through AiRouter/LocalAiProvider in production. */
    private fun classify(message: String): String = when {
        Regex("order|buy|purchase", RegexOption.IGNORE_CASE).containsMatchIn(message) -> "how_to_order"
        Regex("hour|open|close", RegexOption.IGNORE_CASE).containsMatchIn(message) -> "store_hours"
        Regex("status|track", RegexOption.IGNORE_CASE).containsMatchIn(message) -> "order_status"
        else -> "unclassified"
    }
}
