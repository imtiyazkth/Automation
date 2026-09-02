package com.personalai.os.core.security

data class AuditEntry(
    val timestamp: Long,
    val actor: String,           // "head-agent" | agent id | "user"
    val action: String,
    val target: String?,
    val result: String,          // SUCCESS | PARTIAL_SUCCESS | FAILED | REQUIRES_USER_ACTION | BLOCKED
    val detail: String? = null
)

/**
 * Every important action must be logged (blueprint Part 33/37). This is a
 * write-mostly append log; the real implementation persists to the
 * encrypted Room DB (see data/entities/AuditLogEntity.kt) and exposes a
 * read view from the Audit Log screen.
 */
interface AuditLogger {
    fun log(entry: AuditEntry)
    fun recent(limit: Int = 100): List<AuditEntry>
}

class InMemoryAuditLogger : AuditLogger {
    private val entries = mutableListOf<AuditEntry>()
    override fun log(entry: AuditEntry) { entries.add(0, entry) }
    override fun recent(limit: Int): List<AuditEntry> = entries.take(limit)
}
