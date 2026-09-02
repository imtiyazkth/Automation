package com.personalai.os.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.personalai.os.data.entities.AuditLogEntity

@Dao
interface AuditDao {
    @Insert
    suspend fun insert(entry: AuditLogEntity)

    @Query("SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT :limit")
    suspend fun recent(limit: Int = 100): List<AuditLogEntity>
}
