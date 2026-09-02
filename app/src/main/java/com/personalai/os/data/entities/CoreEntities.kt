package com.personalai.os.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

// Mirrors the ER diagram in blueprint Part 24. Split by domain across a
// few files for readability; all live in the same Room database
// (see data/AppDatabase.kt).

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val pinHash: String? = null,
    val biometricEnabled: Boolean = false
)

@Entity(tableName = "agent_permissions")
data class AgentPermissionEntity(
    @PrimaryKey val id: String,          // "<agentId>:<permission>"
    val agentId: String,
    val permission: String,
    val granted: Boolean,
    val grantedBy: String,
    val timestamp: Long
)

@Entity(tableName = "audit_log")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val actor: String,
    val action: String,
    val target: String?,
    val result: String,
    val detail: String?
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "memory")
data class MemoryEntity(
    @PrimaryKey val id: String,
    val level: String,
    val ownerAgentId: String?,
    val key: String,
    val value: String,
    val approvedByUser: Boolean,
    val timestamp: Long
)

@Entity(tableName = "approvals")
data class ApprovalEntity(
    @PrimaryKey val id: String,
    val agentId: String,
    val action: String,
    val reason: String,
    val draftSummary: String,
    val status: String,          // PENDING | SENT | EDITED | IGNORED | ALWAYS_APPROVED
    val timestamp: Long
)
