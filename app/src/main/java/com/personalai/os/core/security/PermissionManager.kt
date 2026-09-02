package com.personalai.os.core.security

/**
 * Tracks which capability-level permissions (blueprint Part 21 table) the
 * user has actually granted - separate from Android's own runtime
 * permission dialogs. An agent can declare it *wants* "access_hr_database"
 * in its agent.json, but it cannot use it until the user has granted that
 * specific capability here, one at a time, from the Permission Center.
 */
class PermissionManager(private val store: PermissionStore) {

    fun isGranted(permission: String): Boolean = store.get(permission)?.granted == true

    fun grant(permission: String, grantedBy: String = "user") {
        store.set(permission, granted = true, grantedBy = grantedBy)
    }

    fun revoke(permission: String) {
        store.set(permission, granted = false, grantedBy = "user")
        // Revocation must immediately pause any agent depending on it -
        // the AgentRegistry/PolicyEngine re-checks on every single call,
        // so there is nothing further to do here for correctness, only
        // for UX (e.g. notifying the user which agents just paused).
    }

    fun grantedPermissions(): List<String> = store.allGranted()
}

/** Minimal persistence contract - back this with the encrypted Room DB in production. */
interface PermissionStore {
    data class Entry(val granted: Boolean, val grantedBy: String, val timestamp: Long = System.currentTimeMillis())
    fun get(permission: String): Entry?
    fun set(permission: String, granted: Boolean, grantedBy: String)
    fun allGranted(): List<String>
}

/** In-memory reference implementation, useful for tests and early bring-up. */
class InMemoryPermissionStore : PermissionStore {
    private val map = mutableMapOf<String, PermissionStore.Entry>()
    override fun get(permission: String) = map[permission]
    override fun set(permission: String, granted: Boolean, grantedBy: String) {
        map[permission] = PermissionStore.Entry(granted, grantedBy)
    }
    override fun allGranted(): List<String> = map.filter { it.value.granted }.keys.toList()
}
