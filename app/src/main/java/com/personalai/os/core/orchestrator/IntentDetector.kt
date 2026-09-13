package com.personalai.os.core.orchestrator

import com.personalai.os.core.ai.AiRouter

class IntentDetector(private val aiRouter: AiRouter) {

    private val fallbackRules: List<Pair<Regex, String>> = listOf(
        Regex("what can you do|help me|show help|capabilities|what commands", RegexOption.IGNORE_CASE) to "help",
        Regex("evaluate this job|should i apply|rate this (job|offer)|review this (job|offer)|is this job (good|worth)", RegexOption.IGNORE_CASE) to "job_evaluate",
        Regex("absent|on leave|overtime|attendance|payroll|resign", RegexOption.IGNORE_CASE) to "hr_query",
        Regex("job|resume|vacancy|hiring|posted.*hour", RegexOption.IGNORE_CASE) to "job_search",
        Regex("pdf|excel|convert|extract.*table|ocr|spreadsheet", RegexOption.IGNORE_CASE) to "document_task",
        Regex("check this link|is this (a )?scam|fraudulent|phishing", RegexOption.IGNORE_CASE) to "link_check",
        Regex("whatsapp|telegram|sms|send.*message|tell .* that", RegexOption.IGNORE_CASE) to "send_message",
        Regex("marketing mode|automation.*(on|off)|turn (on|off)", RegexOption.IGNORE_CASE) to "automation_toggle",
        Regex("play|youtube|video", RegexOption.IGNORE_CASE) to "media_search",
        Regex("order|orders today|crm|lead", RegexOption.IGNORE_CASE) to "crm_query",
        Regex("wifi|bluetooth|open camera|open settings|lock the device|set alarm", RegexOption.IGNORE_CASE) to "device_action",
        Regex("stop all automation|pause everything|kill switch", RegexOption.IGNORE_CASE) to "emergency_control"
    )

    suspend fun detect(text: String): DetectedIntent {
        val routed = runCatching { aiRouter.classifyIntent(text) }.getOrNull()
        if (routed != null && routed.confidence >= 0.55) return routed

        val match = fallbackRules.firstOrNull { it.first.containsMatchIn(text) }
        return DetectedIntent(
            intentType = match?.second ?: "unknown",
            confidence = if (match != null) 0.6 else 0.2,
            slots = emptyMap(),
            rawText = text,
            source = "rule_based_fallback"
        )
    }
}
