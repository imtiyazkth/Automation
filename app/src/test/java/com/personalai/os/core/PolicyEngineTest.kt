package com.personalai.os.core

import com.personalai.os.core.agents.AgentDefinition
import com.personalai.os.core.automation.AutomationMode
import com.personalai.os.core.security.InMemoryPermissionStore
import com.personalai.os.core.security.PermissionManager
import com.personalai.os.core.security.PolicyDecision
import com.personalai.os.core.security.PolicyEngine
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PolicyEngineTest {

    private lateinit var permissionManager: PermissionManager
    private lateinit var policyEngine: PolicyEngine

    private val sampleAgent = AgentDefinition(
        id = "hr-agent", name = "HR Agent", category = "hr", description = "",
        capabilities = listOf("hr_query"), model = "local:1-3B",
        tools = listOf("hr_database_read"), permissions = listOf("access_hr_database"),
        risk = "high", automation_mode = "manual",
        input_schema = "HrQuery", output_schema = "HrReport",
        requires_approval_for = listOf("export_full_payroll")
    )

    @Before
    fun setUp() {
        permissionManager = PermissionManager(InMemoryPermissionStore())
        policyEngine = PolicyEngine(permissionManager)
    }

    @Test
    fun `denies when required permission is not granted`() {
        val decision = policyEngine.evaluate(sampleAgent, "query", AutomationMode.FULL)
        assertTrue(decision is PolicyDecision.Deny)
    }

    @Test
    fun `requires approval for actions explicitly flagged in agent definition`() {
        permissionManager.grant("access_hr_database")
        val decision = policyEngine.evaluate(sampleAgent, "export_full_payroll", AutomationMode.FULL)
        assertTrue(decision is PolicyDecision.RequireApproval)
    }

    @Test
    fun `manual mode requires approval even for granted, unflagged actions`() {
        permissionManager.grant("access_hr_database")
        val decision = policyEngine.evaluate(sampleAgent, "query", AutomationMode.MANUAL)
        assertTrue(decision is PolicyDecision.RequireApproval)
    }

    @Test
    fun `smart mode still requires approval for high-risk agents`() {
        permissionManager.grant("access_hr_database")
        val decision = policyEngine.evaluate(sampleAgent, "query", AutomationMode.SMART)
        assertTrue(decision is PolicyDecision.RequireApproval)
    }

    @Test
    fun `full mode allows granted, unflagged, non-critical-risk actions`() {
        permissionManager.grant("access_hr_database")
        val decision = policyEngine.evaluate(sampleAgent, "query", AutomationMode.FULL)
        assertTrue(decision is PolicyDecision.Allow)
    }

    @Test
    fun `unknown recipient always requires approval regardless of mode`() {
        permissionManager.grant("access_hr_database")
        val decision = policyEngine.evaluate(sampleAgent, "query", AutomationMode.FULL, isNewOrUnknownRecipient = true)
        assertTrue(decision is PolicyDecision.RequireApproval)
    }
}
