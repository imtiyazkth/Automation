package com.personalai.os.core.automation

/**
 * Blueprint Part 5 - Human-in-the-Loop modes.
 * MANUAL: every significant action requires explicit approval.
 * SMART:  previously-approved rules may fire automatically; high-risk
 *         actions still require approval (see PolicyEngine).
 * FULL:   only for explicitly trusted workflows; even then, critical-risk
 *         actions are never fully automated.
 */
enum class AutomationMode { MANUAL, SMART, FULL }

interface AutomationModeStore {
    fun globalMode(): AutomationMode
    fun setGlobalMode(mode: AutomationMode)
    fun modeFor(agentId: String): AutomationMode
    fun setModeFor(agentId: String, mode: AutomationMode)
}

class InMemoryAutomationModeStore : AutomationModeStore {
    private var global: AutomationMode = AutomationMode.MANUAL
    private val perAgent = mutableMapOf<String, AutomationMode>()

    override fun globalMode(): AutomationMode = global
    override fun setGlobalMode(mode: AutomationMode) { global = mode }
    override fun modeFor(agentId: String): AutomationMode = perAgent[agentId] ?: global
    override fun setModeFor(agentId: String, mode: AutomationMode) { perAgent[agentId] = mode }
}
