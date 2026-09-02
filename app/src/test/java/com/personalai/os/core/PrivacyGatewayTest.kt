package com.personalai.os.core

import com.personalai.os.core.ai.DataSensitivity
import com.personalai.os.core.ai.PrivacyGateway
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrivacyGatewayTest {

    private val gateway = PrivacyGateway()

    @Test
    fun `clears allowlisted fields for job search`() {
        val decision = gateway.evaluate("job_search", mapOf("skills" to "kotlin, android"))
        assertTrue(decision.cleared)
        assertEquals(DataSensitivity.PUBLIC, decision.sensitivity)
    }

    @Test
    fun `blocks sensitive fields not on the allowlist`() {
        val decision = gateway.evaluate("job_search", mapOf("skills" to "kotlin", "phone" to "+974..."))
        assertFalse(decision.cleared)
        assertEquals(DataSensitivity.SENSITIVE_NEEDS_CONSENT, decision.sensitivity)
    }

    @Test
    fun `blocks intents with no allowlist defined at all`() {
        val decision = gateway.evaluate("hr_query", mapOf("employeeName" to "Imtiyaz"))
        assertFalse(decision.cleared)
        assertEquals(DataSensitivity.SENSITIVE_BLOCKED, decision.sensitivity)
    }
}
