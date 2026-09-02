package com.personalai.os.core.orchestrator

/** Structured intent produced by IntentDetector — the output of "understanding". */
data class DetectedIntent(
    val intentType: String,           // e.g. "hr_query", "device_action", "job_search"
    val confidence: Double,           // 0.0-1.0, from whichever provider produced it
    val slots: Map<String, String>,   // extracted parameters: recipient, platform, filters...
    val rawText: String,
    val source: String                // "local" | "gemini" | "rule_based_fallback"
)

/** A single step in a decomposed task graph. */
data class TaskStep(
    val id: String,
    val agentId: String,
    val action: String,
    val params: Map<String, Any?> = emptyMap(),
    val dependsOn: List<String> = emptyList()
)

/** The full plan the Head Agent will execute for one user request. */
data class TaskPlan(
    val originalRequest: String,
    val intent: DetectedIntent,
    val steps: List<TaskStep>
)
