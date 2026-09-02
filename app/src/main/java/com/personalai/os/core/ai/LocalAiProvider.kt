package com.personalai.os.core.ai

import com.personalai.os.core.orchestrator.DetectedIntent

/**
 * ============================================================================
 *  THIS IS THE ONE FILE THAT MOST NEEDS YOUR OWN WORK BEFORE THIS APP DOES
 *  REAL ON-DEVICE AI. Read this comment before changing anything else.
 * ============================================================================
 *
 * There is no bundled local LLM here - a real GGUF model plus a native
 * inference runtime is a multi-hundred-megabyte binary/model pair that
 * can't be scaffolded blind. Wire in ONE of:
 *
 *   1. llama.cpp Android build (github.com/ggerganov/llama.cpp, examples/android)
 *      - build the JNI .so, bundle or download a quantized GGUF model
 *        (1-4B params, Q4_K_M is a good default), call it via JNI here.
 *   2. MLC-LLM Android runtime (github.com/mlc-ai/mlc-llm)
 *      - has an existing Android app target you can adapt instead of
 *        writing your own JNI layer.
 *
 * Until one of those is wired in, [generateText] and [classifyIntent] fall
 * back to deterministic, transparent logic so the REST of the pipeline
 * (planning, permission checks, agent execution, workflows, audit) is fully
 * exercised and testable without pretending to have real model reasoning.
 */
class LocalAiProvider(
    // Swap this for a real JNI/runtime handle once wired in, e.g.:
    // private val runtime: LlamaCppRuntime?
    private val modelLoaded: Boolean = false
) : AiProvider {

    override val name: String = "local"

    override suspend fun classifyIntent(text: String): DetectedIntent {
        if (!modelLoaded) {
            // No real model yet - return low confidence so IntentDetector's
            // rule-based fallback takes over instead of a fabricated guess.
            return DetectedIntent(
                intentType = "unknown",
                confidence = 0.0,
                slots = emptyMap(),
                rawText = text,
                source = "local_unavailable"
            )
        }
        TODO("Call the on-device runtime's classification prompt and parse structured output.")
    }

    override suspend fun generateText(prompt: String, context: Map<String, String>): String {
        if (!modelLoaded) {
            return "[local model not wired in yet - see LocalAiProvider.kt header comment]"
        }
        TODO("Call the on-device runtime's generation prompt.")
    }

    override suspend fun isAvailable(): Boolean = modelLoaded
}
