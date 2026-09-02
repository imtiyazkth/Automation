package com.personalai.os.core.ai

/** Classification outcome for a piece of outbound data. */
enum class DataSensitivity { PUBLIC, NEEDS_ANONYMIZING, SENSITIVE_BLOCKED, SENSITIVE_NEEDS_CONSENT }

data class GatewayDecision(
    val sensitivity: DataSensitivity,
    val cleared: Boolean,
    val redactedPayload: Map<String, String>? = null,
    val reason: String
)

/**
 * Blueprint Part 20. Sits between any agent and [GeminiProvider]. Nothing
 * reaches the cloud provider without passing through here first.
 *
 * This reference implementation uses a simple field-allowlist per intent
 * type - replace/extend with real PII detection (regex + NER) as the
 * project matures, but keep the default posture "block unless cleared".
 */
class PrivacyGateway {

    private val sensitiveFieldNames = setOf(
        "phone", "address", "national_id", "salary", "ssn", "id_number", "dob"
    )

    // Per-intent allowlist of fields that MAY go to the cloud provider.
    private val allowlists = mapOf(
        "job_search" to setOf("skills", "experience_years", "target_role", "target_country", "education_level")
    )

    fun evaluate(intentType: String, payload: Map<String, String>): GatewayDecision {
        val allowed = allowlists[intentType]
        if (allowed == null) {
            return GatewayDecision(DataSensitivity.SENSITIVE_BLOCKED, cleared = false, reason = "No allowlist defined for '$intentType' - defaulting to block")
        }

        val blockedFields = payload.keys.filter { it in sensitiveFieldNames && it !in allowed }
        if (blockedFields.isNotEmpty()) {
            return GatewayDecision(
                DataSensitivity.SENSITIVE_NEEDS_CONSENT,
                cleared = false,
                reason = "Fields require explicit user consent before leaving device: ${blockedFields.joinToString()}"
            )
        }

        val redacted = payload.filterKeys { it in allowed }
        return GatewayDecision(DataSensitivity.PUBLIC, cleared = true, redactedPayload = redacted, reason = "Cleared via '$intentType' allowlist")
    }
}
