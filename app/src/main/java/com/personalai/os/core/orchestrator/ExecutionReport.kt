package com.personalai.os.core.orchestrator

/**
 * Every single action the Head Agent takes must resolve to exactly one of
 * these four states. Silent success is never allowed (blueprint Part 6 / 27).
 */
sealed class ExecutionReport(open val message: String) {
    data class Success(override val message: String, val data: Map<String, Any?> = emptyMap()) :
        ExecutionReport(message)

    data class PartialSuccess(
        override val message: String,
        val completed: List<String>,
        val remaining: List<String>
    ) : ExecutionReport(message)

    data class Failed(override val message: String, val cause: Throwable? = null) :
        ExecutionReport(message)

    data class RequiresUserAction(override val message: String, val reason: String) :
        ExecutionReport(message)
}
