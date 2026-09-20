package com.personalai.os.integrations.telegram

import com.personalai.os.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class TelegramBotClient(
    private val client: OkHttpClient = OkHttpClient(),
    private val token: String = BuildConfig.TELEGRAM_BOT_TOKEN
) {
    suspend fun sendMessage(chatId: String, text: String): String {
        if (token.isBlank()) return "[not configured - set TELEGRAM_BOT_TOKEN]"
        return withContext(Dispatchers.IO) {
            val url = "https://api.telegram.org/bot$token/sendMessage" +
                "?chat_id=$chatId&text=${java.net.URLEncoder.encode(text, "UTF-8")}"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) "sent" else "failed: HTTP ${response.code}"
            }
        }
    }
}
