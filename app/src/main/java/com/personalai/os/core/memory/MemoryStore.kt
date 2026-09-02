package com.personalai.os.core.memory

/**
 * Blueprint Part 22 - multiple memory levels. This module intentionally
 * only stores memory the user has explicitly approved (e.g. "when I'm busy,
 * tell people I'll reply later"); it must never silently persist inferred
 * preferences. View/edit/delete/disable/export are first-class operations,
 * not afterthoughts.
 */
enum class MemoryLevel { SHORT_TERM, TASK, PREFERENCE, LONG_TERM, AGENT }

data class MemoryEntry(
    val id: String,
    val level: MemoryLevel,
    val ownerAgentId: String?,   // null = shared/user-level memory
    val key: String,
    val value: String,
    val approvedByUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

interface MemoryStore {
    fun save(entry: MemoryEntry)
    fun get(key: String, level: MemoryLevel): MemoryEntry?
    fun all(level: MemoryLevel? = null): List<MemoryEntry>
    fun delete(id: String)
    fun disableLevel(level: MemoryLevel)
}

class InMemoryMemoryStore : MemoryStore {
    private val entries = mutableMapOf<String, MemoryEntry>()
    private val disabledLevels = mutableSetOf<MemoryLevel>()

    override fun save(entry: MemoryEntry) {
        require(entry.approvedByUser) { "Memory can only be persisted after explicit user approval." }
        if (entry.level in disabledLevels) return
        entries[entry.id] = entry
    }
    override fun get(key: String, level: MemoryLevel): MemoryEntry? =
        entries.values.firstOrNull { it.key == key && it.level == level }
    override fun all(level: MemoryLevel?): List<MemoryEntry> =
        entries.values.filter { level == null || it.level == level }
    override fun delete(id: String) { entries.remove(id) }
    override fun disableLevel(level: MemoryLevel) {
        disabledLevels.add(level)
        entries.values.filter { it.level == level }.forEach { entries.remove(it.id) }
    }
}
