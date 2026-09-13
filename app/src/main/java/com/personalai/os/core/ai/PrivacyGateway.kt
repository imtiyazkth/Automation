package com.personalai.os.core.ai

enum class DataSensitivity { PUBLIC, NEEDS_ANONYMIZING, SENSITIVE_BLOCKED, SENSITIVE_NEEDS_CONSENT }

data class GatewayDecision(
    val sensitivity: DataSensitivity,
    val cleared: Boolean,
    val redactedPayload: Map<String, String>? = null,
    val reason: String
)

class PrivacyGateway {

    private val sensitiveFieldNames = setOf(
        "phone", "address", "national_id", "salary", "ssn", "id_number", "dob"
    )

    private val allowlists = mapOf(
        "job_search" to setOf(
            "skills", "experience_years", "target_role", "target_country", "education_level",
            "job_description", "candidate_skills"
        )
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
