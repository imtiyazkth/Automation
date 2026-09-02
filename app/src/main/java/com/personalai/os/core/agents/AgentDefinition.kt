package com.personalai.os.core.agents

/**
 * Mirrors the agent.json schema from the blueprint (Part 24). This is a
 * *descriptive* registry entry - identity, permissions, risk, approval
 * rules - loaded from assets/agents/*.json. It is deliberately separate
 * from the [Agent] interface (the executable behavior): the Head Agent
 * checks permissions/risk against this definition BEFORE ever invoking
 * the matching Agent implementation.
 */
data class AgentDefinition(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val capabilities: List<String>,
    val model: String,
    val tools: List<String>,
    val permissions: List<String>,
    val risk: String,                       // "low" | "medium" | "high" | "critical"
    val automation_mode: String,            // "manual" | "smart" | "full"
    val input_schema: String,
    val output_schema: String,
    val requires_approval_for: List<String> = emptyList(),
    val scope_restriction: String? = null,
    val data_residency: String? = null
)
