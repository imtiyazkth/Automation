package com.personalai.os.core.ai

import android.util.Base64
import com.personalai.os.BuildConfig
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed class GeminiLiveEvent {
    object SetupComplete : GeminiLiveEvent()
    data class TextDelta(val text: String) : GeminiLiveEvent()
    object TurnComplete : GeminiLiveEvent()
    data class Error(val message: String) : GeminiLiveEvent()
    object Closed : GeminiLiveEvent()
}

class GeminiLiveClient(
    private val apiKey: String = BuildConfig.GEMINI_API_KEY,
    private val model: String = "models/gemini-2.0-flash-live-001"
) {
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.SECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private val _events = MutableSharedFlow<GeminiLiveEvent>(extraBufferCapacity = 64)
    val events: SharedFlow<GeminiLiveEvent> = _events

    fun isConfigured(): Boolean = apiKey.isNotBlank()

    fun connect() {
        if (!isConfigured()) {
            _events.tryEmit(GeminiLiveEvent.Error("GEMINI_API_KEY not set - see local.properties"))
            return
        }
        val url = "wss://generativelanguage.googleapis.com/ws/" +
            "google.ai.generativelanguage.v1beta.GenerativeService.BidiGenerateContent?key=$apiKey"
        val request = Request.Builder().url(url).build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                val setup = JSONObject().apply {
                    put("setup", JSONObject().apply {
                        put("model", model)
                        put("generationConfig", JSONObject().apply {
                            put("responseModalities", JSONArray().put("TEXT"))
                        })
                        put("systemInstruction", JSONObject().apply {
                            put("parts", JSONArray().put(JSONObject().apply {
                                put(
                                    "text",
                                    "You are a transcription relay for a personal automation " +
                                        "assistant. The user is speaking a command out loud. " +
                                        "Reply with ONLY their command, transcribed as plain " +
                                        "text - no commentary, no answering the command " +
                                        "yourself, no greetings. If they trail off or it's " +
                                        "unclear, transcribe your best guess of what they said."
                                )
                            }))
                        })
                    })
                }
                webSocket.send(setup.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleServerMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _events.tryEmit(GeminiLiveEvent.Error(t.message ?: "WebSocket failure"))
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _events.tryEmit(GeminiLiveEvent.Closed)
            }
        })
    }

    fun sendAudioChunk(pcm16Bytes: ByteArray) {
        val base64 = Base64.encodeToString(pcm16Bytes, Base64.NO_WRAP)
        val message = JSONObject().apply {
            put("realtimeInput", JSONObject().apply {
                put("mediaChunks", JSONArray().put(JSONObject().apply {
                    put("mimeType", "audio/pcm;rate=16000")
                    put("data", base64)
                }))
            })
        }
        webSocket?.send(message.toString())
    }

    fun close() {
        webSocket?.close(1000, "done")
        webSocket = null
    }

    private fun handleServerMessage(raw: String) {
        val json = runCatching { JSONObject(raw) }.getOrNull() ?: return

        if (json.has("setupComplete")) {
            _events.tryEmit(GeminiLiveEvent.SetupComplete)
            return
        }

        val serverContent = json.optJSONObject("serverContent") ?: return
        val modelTurn = serverContent.optJSONObject("modelTurn")
        val parts = modelTurn?.optJSONArray("parts")
        if (parts != null) {
            for (i in 0 until parts.length()) {
                val part = parts.optJSONObject(i) ?: continue
                val text = part.optString("text", "")
                if (text.isNotEmpty()) _events.tryEmit(GeminiLiveEvent.TextDelta(text))
            }
        }
        if (serverContent.optBoolean("turnComplete", false)) {
            _events.tryEmit(GeminiLiveEvent.TurnComplete)
        }
    }
}
