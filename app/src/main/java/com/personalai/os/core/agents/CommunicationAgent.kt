package com.personalai.os.core.agents

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.personalai.os.core.orchestrator.ExecutionReport
import com.personalai.os.core.orchestrator.TaskStep
import com.personalai.os.integrations.telegram.TelegramBotClient
import com.personalai.os.integrations.whatsapp.WhatsAppBusinessClient

class CommunicationAgent(
    private val whatsAppBusinessClient: WhatsAppBusinessClient,
    private val telegramClient: TelegramBotClient,
    private val appContext: Context
) : Agent {
    override val definitionId = "communication-agent"

    override suspend fun execute(step: TaskStep): ExecutionReport {
        return when (step.action) {
            "send" -> send(step)
            else -> ExecutionReport.Failed("CommunicationAgent has no handler for action '${step.action}'")
        }
    }

    private suspend fun send(step: TaskStep): ExecutionReport {
        val recipient = step.params["recipient"] as? String
        val message = step.params["message"] as? String
        if (recipient == null || message == null) {
            return ExecutionReport.RequiresUserAction("Who should I send this to, and what should it say?", "missing recipient/message")
        }

        return when ((step.params["platform"] as? String)?.lowercase() ?: "whatsapp") {
            "whatsapp" -> sendViaPersonalWhatsApp(recipient, message)
            "whatsapp_business" -> sendViaWhatsAppBusiness(recipient, message)
            "telegram" -> sendViaTelegram(recipient, message)
            "sms" -> ExecutionReport.Failed("SMS sending isn't wired up yet - only WhatsApp (personal/Business) and Telegram bot are implemented so far.")
            else -> ExecutionReport.Failed("Unrecognized platform for sending a message.")
        }
    }

    private fun sendViaPersonalWhatsApp(recipient: String, message: String): ExecutionReport {
        if (!looksLikePhoneNumber(recipient)) {
            return ExecutionReport.RequiresUserAction(
                "I don't have contacts lookup yet, so I need their phone number (with country code) instead of just \"$recipient\".",
                "missing recipient"
            )
        }
        val digitsOnly = recipient.replace(Regex("[^\\d]"), "")
        val uri = Uri.parse("https://wa.me/$digitsOnly?text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
        return runCatching { appContext.startActivity(intent) }
            .fold(
                onSuccess = { ExecutionReport.Success("Opened WhatsApp with your message ready for +$digitsOnly \u2014 review and tap Send to actually deliver it.") },
                onFailure = { ExecutionReport.Failed("Couldn't open WhatsApp - is it installed?", it) }
            )
    }

    private suspend fun sendViaWhatsAppBusiness(recipient: String, message: String): ExecutionReport {
        val result = runCatching { whatsAppBusinessClient.sendMessage(recipient, message) }
            .getOrElse { return ExecutionReport.Failed("WhatsApp Business API call threw an error", it) }
        return when {
            result == "sent" -> ExecutionReport.Success("Message sent to $recipient via WhatsApp Business API")
            result.startsWith("[not configured") ->
                ExecutionReport.Failed("WhatsApp Business API isn't configured (missing token/phone number ID). Say \"send a WhatsApp message\" without \"business\" to use your personal WhatsApp instead.")
            else -> ExecutionReport.Failed("WhatsApp Business API call failed: $result")
        }
    }

    private suspend fun sendViaTelegram(recipient: String, message: String): ExecutionReport {
        val result = runCatching { telegramClient.sendMessage(recipient, message) }
            .getOrElse { return ExecutionReport.Failed("Telegram call threw an error", it) }
        return when {
            result == "sent" -> ExecutionReport.Success("Message sent to $recipient via Telegram bot")
            result.startsWith("[not configured") ->
                ExecutionReport.Failed("Telegram bot isn't configured yet (missing TELEGRAM_BOT_TOKEN). Also: a Telegram bot can only message people who've already started a chat with it - it can't message an arbitrary name or username directly.")
            else -> ExecutionReport.Failed("Telegram send failed: $result")
        }
    }

    private fun looksLikePhoneNumber(text: String): Boolean =
        text.replace(Regex("[\\s()-]"), "").matches(Regex("^\\+?\\d{7,15}$"))
}
