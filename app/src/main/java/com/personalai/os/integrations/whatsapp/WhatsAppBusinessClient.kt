package com.personalai.os.integrations.whatsapp

import com.personalai.os.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

/**
 * Official WhatsApp Business Platform (Cloud API) client - NOT unofficial
 * WhatsApp automation. Requires a Meta Business account, a verified phone
 * number, and WHATSAPP_CLOUD_API_TOKEN / WHATSAPP_PHONE_NUMBER_ID in
 * local.properties.
 *
 * IMPORTANT (blueprint Part 37): as of Jan 15 2026, Meta's platform rules
 * ban general-purpose / open-domain AI chatbots on this API. Only send
 * messages that MarketingAgent has already scoped to an approved business
 * intent (order status, product FAQ, store hours, how-to-order). Do not
 * use this client as a raw "send anything the model generated" pipe.
 */
class WhatsAppBusinessClient(
    private val client: OkHttpClient = OkHttpClient(),
    private val token: String = BuildConfig.WHATSAPP_CLOUD_API_TOKEN,
    private val phoneNumberId: String = BuildConfig.WHATSAPP_PHONE_NUMBER_ID
) {
    private val endpoint get() = "https://graph.facebook.com/v20.0/$phoneNumberId/messages"

    fun sendMessage(toE164: String, body: String): String {
        if (token.isBlank() || phoneNumberId.isBlank()) {
            return "[not configured - set WHATSAPP_CLOUD_API_TOKEN / WHATSAPP_PHONE_NUMBER_ID]"
        }
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
            return if (response.isSuccessful) "sent" else "failed: HTTP ${response.code}"
        }
    }
}
