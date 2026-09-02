package com.personalai.os.core.ai

import com.personalai.os.core.orchestrator.DetectedIntent

/**
 * Blueprint Part 10 - a simple, auditable routing table rather than a
 * black box: intent type -> provider. Private/offline-capable intents go
 * local; anything needing the live internet goes to Gemini, and only after
 * the PrivacyGateway has cleared the payload.
 */
class AiRouter(
    private val local: LocalAiProvider,
    private val cloud: GeminiProvider,
    private val privacyGateway: PrivacyGateway
) {
    private val cloudRoutedIntents = setOf("job_search", "web_search", "media_search", "link_check")

    suspend fun classifyIntent(text: String): DetectedIntent {
        // Intent understanding is always local-first - see GeminiProvider comment.
        return local.classifyIntent(text)
    }

    suspend fun generate(intentType: String, prompt: String, payload: Map<String, String> = emptyMap()): String {
        return if (intentType in cloudRoutedIntents) {
            val decision = privacyGateway.evaluate(intentType, payload)
            if (!decision.cleared) return "[blocked by privacy gateway: ${decision.reason}]"
            cloud.generateText(prompt, decision.redactedPayload.orEmpty())
        } else {
            local.generateText(prompt, payload)
        }
    }
}
