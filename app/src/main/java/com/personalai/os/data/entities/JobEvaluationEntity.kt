package com.personalai.os.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "job_evaluations")
data class JobEvaluationEntity(
    @PrimaryKey val id: String,
    val jobTitle: String?,
    val company: String?,
    val score: Double?,
    val reportText: String,
    val sourceUrl: String?,
    val timestamp: Long
)
