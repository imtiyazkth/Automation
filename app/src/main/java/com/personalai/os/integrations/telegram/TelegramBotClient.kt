package com.personalai.os.integrations.telegram

import com.personalai.os.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.Request

/**
 * Official Telegram Bot API client. Covers chats with the bot only - NOT a
 * general "userbot" automating your personal account (see blueprint Part 37
 * on why that path is a ToS gray area we deliberately don't build).
 */
class TelegramBotClient(
    private val client: OkHttpClient = OkHttpClient(),
    private val token: String = BuildConfig.TELEGRAM_BOT_TOKEN
) {
    fun sendMessage(chatId: String, text: String): String {
        if (token.isBlank()) return "[not configured - set TELEGRAM_BOT_TOKEN]"
        val url = "https://api.telegram.org/bot$token/sendMessage" +
            "?chat_id=$chatId&text=${java.net.URLEncoder.encode(text, "UTF-8")}"
        val request = Request.Builder().url(url).get().build()
        client.newCall(request).execute().use { response ->
            return if (response.isSuccessful) "sent" else "failed: HTTP ${response.code}"
        }
    }
}
