package com.personalai.os.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.personalai.os.data.entities.JobEvaluationEntity

@Dao
interface JobEvaluationDao {
    @Insert
    suspend fun insert(entity: JobEvaluationEntity)

    @Query("SELECT * FROM job_evaluations ORDER BY timestamp DESC LIMIT :limit")
    suspend fun recent(limit: Int = 50): List<JobEvaluationEntity>
}
