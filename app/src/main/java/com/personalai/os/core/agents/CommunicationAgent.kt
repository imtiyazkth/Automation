package com.personalai.os.core.agents

import com.personalai.os.core.orchestrator.ExecutionReport
import com.personalai.os.core.orchestrator.TaskStep
import com.personalai.os.integrations.whatsapp.WhatsAppBusinessClient

class CommunicationAgent(private val whatsAppClient: WhatsAppBusinessClient) : Agent {
    override val definitionId = "communication-agent"

    override suspend fun execute(step: TaskStep): ExecutionReport {
        return when (step.action) {
            "send" -> {
                val recipient = step.params["recipient"] as? String
                val message = step.params["message"] as? String
                if (recipient == null || message == null) {
                    return ExecutionReport.RequiresUserAction("Who should I send this to, and what should it say?", "missing recipient/message")
                }
                val sendResult = runCatching { whatsAppClient.sendMessage(recipient, message) }
                    .getOrElse { return ExecutionReport.Failed("Send failed", it) }
                ExecutionReport.Success("Message sent to $recipient", mapOf("providerResult" to sendResult))
            }
            else -> ExecutionReport.Failed("CommunicationAgent has no handler for action '${step.action}'")
        }
    }
}
