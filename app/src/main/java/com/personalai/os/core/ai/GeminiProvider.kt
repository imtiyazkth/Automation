package com.personalai.os.core.ai

import com.personalai.os.BuildConfig
import com.personalai.os.core.orchestrator.DetectedIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class GeminiProvider(
    private val client: OkHttpClient = OkHttpClient(),
    private val apiKey: String = BuildConfig.GEMINI_API_KEY,
    private val model: String = "gemini-2.0-flash"
) : AiProvider {

    override val name: String = "gemini"

    private val endpoint: String
        get() = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"

    override suspend fun classifyIntent(text: String): DetectedIntent {
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

        return withContext(Dispatchers.IO) {
            val body = JSONObject().apply {
                put("contents", listOf(
                    JSONObject().apply {
                        put("parts", listOf(JSONObject().apply { put("text", prompt) }))
                    }
                ))
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder().url(endpoint).post(body).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext "[Gemini call failed: HTTP ${response.code}]"
                val json = JSONObject(response.body?.string().orEmpty())
                runCatching {
                    json.getJSONArray("candidates").getJSONObject(0)
                        .getJSONObject("content").getJSONArray("parts")
                        .getJSONObject(0).getString("text")
                }.getOrDefault("[Unexpected Gemini response shape]")
            }
        }
    }

    override suspend fun isAvailable(): Boolean = apiKey.isNotBlank()
}
