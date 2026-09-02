package com.personalai.os.core

import com.personalai.os.core.workflows.Condition
import com.personalai.os.core.workflows.ConditionEvaluator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConditionEvaluatorTest {

    private val evaluator = ConditionEvaluator()

    @Test
    fun `equality condition matches`() {
        val result = evaluator.evaluate(Condition("mode == 'ON'"), mapOf("mode" to "ON"))
        assertTrue(result)
    }

    @Test
    fun `equality condition does not match different value`() {
        val result = evaluator.evaluate(Condition("mode == 'ON'"), mapOf("mode" to "OFF"))
        assertFalse(result)
    }

    @Test
    fun `inequality condition matches`() {
        val result = evaluator.evaluate(Condition("mode != 'OFF'"), mapOf("mode" to "ON"))
        assertTrue(result)
    }
}
