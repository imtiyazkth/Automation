package com.personalai.os.core.workflows

/** WHEN / IF / THEN structure from blueprint Part 23. */
data class Trigger(val type: String, val params: Map<String, String> = emptyMap())
data class Condition(val expression: String)  // simple boolean expression, e.g. "mode == 'ON'"
data class Action(val agentId: String, val action: String, val params: Map<String, String> = emptyMap())

data class Rule(
    val id: String,
    val name: String,
    val trigger: Trigger,
    val conditions: List<Condition> = emptyList(),
    val actions: List<Action>
)

data class Workflow(
    val id: String,
    val name: String,
    val enabled: Boolean,
    val rules: List<Rule>
)
