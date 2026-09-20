package com.personalai.os.integrations.whatsapp

import com.personalai.os.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class WhatsAppBusinessClient(
    private val client: OkHttpClient = OkHttpClient(),
    private val token: String = BuildConfig.WHATSAPP_CLOUD_API_TOKEN,
    private val phoneNumberId: String = BuildConfig.WHATSAPP_PHONE_NUMBER_ID
) {
    private val endpoint get() = "https://graph.facebook.com/v20.0/$phoneNumberId/messages"

    suspend fun sendMessage(toE164: String, body: String): String {
        if (token.isBlank() || phoneNumberId.isBlank()) {
            return "[not configured - set WHATSAPP_CLOUD_API_TOKEN / WHATSAPP_PHONE_NUMBER_ID]"
        }
        return withContext(Dispatchers.IO) {
            val payload = JSONObject().apply {
                put("messaging_product", "whatsapp")
                put("to", toE164)
                put("type", "text")
                put("text", JSONObject().apply { put("body", body) })
            }.toString().toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer $token")
                .post(payload)
                .build()

            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) "sent" else "failed: HTTP ${response.code}"
            }
        }
    }
}
