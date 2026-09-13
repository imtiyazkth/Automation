package com.personalai.os.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.personalai.os.data.entities.MessageEntity

@Dao
interface MessageDao {
    @Insert
    suspend fun insert(message: MessageEntity)

    @Query("SELECT COUNT(*) FROM messages WHERE timestamp >= :sinceEpochMillis")
    suspend fun countSince(sinceEpochMillis: Long): Int

    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT :limit")
    suspend fun recent(limit: Int = 50): List<MessageEntity>
}
