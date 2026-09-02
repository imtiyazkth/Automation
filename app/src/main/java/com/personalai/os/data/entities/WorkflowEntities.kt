package com.personalai.os.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workflows")
data class WorkflowEntity(
    @PrimaryKey val id: String,
    val name: String,
    val enabled: Boolean,
    val definitionJson: String   // serialized Workflow (see core/workflows/Workflow.kt)
)

@Entity(tableName = "rules")
data class RuleEntity(
    @PrimaryKey val id: String,
    val workflowId: String,
    val triggerType: String,
    val conditionJson: String?,
    val actionJson: String
)
