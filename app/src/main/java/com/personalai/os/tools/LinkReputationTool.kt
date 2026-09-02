package com.personalai.os.tools

data class ReputationResult(val knownMalicious: Boolean, val source: String)

/**
 * Placeholder reputation lookup. Wire to a real threat-intel feed / safe
 * browsing API of your choice. Must always distinguish "not found in feed"
 * from "confirmed safe" - see LinkSafetyAgent's verdict wording.
 */
class LinkReputationTool {
    fun lookup(host: String): ReputationResult? {
        // TODO: call a real reputation/safe-browsing API here.
        return null // "no data" - LinkSafetyAgent already treats null as UNKNOWN, not SAFE.
    }
}
