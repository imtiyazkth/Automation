package com.personalai.os.core

import com.personalai.os.core.ai.AiRouter
import com.personalai.os.core.ai.GeminiProvider
import com.personalai.os.core.ai.LocalAiProvider
import com.personalai.os.core.ai.PrivacyGateway
import com.personalai.os.core.orchestrator.IntentDetector
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** Exercises the rule-based fallback path (no model loaded). */
class IntentDetectorFallbackTest {

    private val router = AiRouter(LocalAiProvider(modelLoaded = false), GeminiProvider(apiKey = ""), PrivacyGateway())
    private val detector = IntentDetector(router)

    @Test
    fun `classifies an HR-style question via fallback rules`() = runTest {
        val result = detector.detect("Who is absent today?")
        assertEquals("hr_query", result.intentType)
        assertEquals("rule_based_fallback", result.source)
    }

    @Test
    fun `classifies a link safety question via fallback rules`() = runTest {
        val result = detector.detect("Check this link, is it a scam?")
        assertEquals("link_check", result.intentType)
    }

    @Test
    fun `falls back to unknown for unrecognized text`() = runTest {
        val result = detector.detect("asdkjfh qwoeiruqwoeiru")
        assertEquals("unknown", result.intentType)
    }
}
