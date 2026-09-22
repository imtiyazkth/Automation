package com.personalai.os.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scheduled_tasks")
data class ScheduledTaskEntity(
    @PrimaryKey val id: String,
    val name: String,
    val commandText: String,
    val intervalMinutes: Long,
    val enabled: Boolean,
    val createdAt: Long,
    val lastRunAt: Long? = null,
    val lastResultSummary: String? = null
)
