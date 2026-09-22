package com.personalai.os.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.personalai.os.data.entities.ScheduledTaskEntity

@Dao
interface ScheduledTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: ScheduledTaskEntity)

    @Query("SELECT * FROM scheduled_tasks ORDER BY createdAt DESC")
    suspend fun all(): List<ScheduledTaskEntity>

    @Query("SELECT * FROM scheduled_tasks WHERE id = :id")
    suspend fun get(id: String): ScheduledTaskEntity?

    @Query("DELETE FROM scheduled_tasks WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE scheduled_tasks SET lastRunAt = :timestamp, lastResultSummary = :summary WHERE id = :id")
    suspend fun recordRun(id: String, timestamp: Long, summary: String)
}
