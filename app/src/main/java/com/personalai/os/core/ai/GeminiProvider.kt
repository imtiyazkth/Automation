package com.personalai.os.core.ai

import com.personalai.os.BuildConfig
import com.personalai.os.core.orchestrator.DetectedIntent
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Thin REST client for Gemini. Requires GEMINI_API_KEY in local.properties
 * (see app/build.gradle.kts) - never hardcode a real key in source.
 * Used only for tasks the Privacy Gateway has cleared (Part 20): web
 * search, job search, YouTube discovery, link reputation lookups.
 */
class GeminiProvider(
    private val client: OkHttpClient = OkHttpClient(),
    private val apiKey: String = BuildConfig.GEMINI_API_KEY,
    private val model: String = "gemini-2.0-flash"
) : AiProvider {

    override val name: String = "gemini"

    private val endpoint: String
        get() = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

    override suspend fun classifyIntent(text: String): DetectedIntent {
        // Intent classification is intentionally NOT routed to Gemini by
        // default (blueprint Part 10) - it's private user input and the
        // Local provider should handle it. This override exists only for
        // completeness / testing against a cloud baseline if explicitly
        // requested by the user.
        val raw = generateText(
            "Classify this request into a short intent label and return ONLY the label: \"$text\""
        )
        return DetectedIntent(
            intentType = raw.trim().lowercase(),
            confidence = 0.5,
            slots = emptyMap(),
            rawText = text,
            source = "gemini"
        )
    }

    override suspend fun generateText(prompt: String, context: Map<String, String>): String {
        if (apiKey.isBlank()) return "[GEMINI_API_KEY not set - see local.properties]"

        val body = JSONObject().apply {
            put("contents", listOf(
                JSONObject().apply {
                    put("parts", listOf(JSONObject().apply { put("text", prompt) }))
                }
            ))
        }.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder().url(endpoint).post(body).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return "[Gemini call failed: HTTP ${response.code}]"
            val json = JSONObject(response.body?.string().orEmpty())
            return runCatching {
                json.getJSONArray("candidates").getJSONObject(0)
                    .getJSONObject("content").getJSONArray("parts")
                    .getJSONObject(0).getString("text")
            }.getOrDefault("[Unexpected Gemini response shape]")
        }
    }

    override suspend fun isAvailable(): Boolean = apiKey.isNotBlank()
}
