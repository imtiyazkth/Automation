package com.personalai.os.core.ai

import com.personalai.os.core.orchestrator.DetectedIntent

/** Common contract both the local runtime and Gemini implement. */
interface AiProvider {
    val name: String
    suspend fun classifyIntent(text: String): DetectedIntent
    suspend fun generateText(prompt: String, context: Map<String, String> = emptyMap()): String
    suspend fun isAvailable(): Boolean
}
