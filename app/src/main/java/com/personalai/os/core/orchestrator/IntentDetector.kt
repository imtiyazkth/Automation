package com.personalai.os.core.orchestrator

import com.personalai.os.core.ai.AiRouter

/**
 * Maps free text (already transcribed if it came from voice) to a
 * [DetectedIntent]. The real implementation should call AiRouter, which in
 * turn prefers the on-device model. Until a real local LLM runtime is wired
 * in (see core/ai/LocalAiProvider.kt), this falls back to transparent
 * keyword rules - deliberately simple and auditable rather than guessing.
 */
class IntentDetector(private val aiRouter: AiRouter) {

    // Keyword -> intent map used by the rule-based fallback. Order matters:
    // first match wins, so more specific phrases should be checked first.
    private val fallbackRules: List<Pair<Regex, String>> = listOf(
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
        // Preferred path: let the routed AI provider classify (local model
        // when available, since intent understanding is private/offline-first).
        val routed = runCatching { aiRouter.classifyIntent(text) }.getOrNull()
        if (routed != null && routed.confidence >= 0.55) return routed

        // Fallback: transparent rule-based classification. Confidence is
        // intentionally modest so downstream planning treats it cautiously.
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
