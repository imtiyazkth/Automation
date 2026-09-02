package com.personalai.os.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.personalai.os.data.entities.AgentPermissionEntity

@Dao
interface PermissionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AgentPermissionEntity)

    @Query("SELECT * FROM agent_permissions WHERE permission = :permission AND granted = 1 LIMIT 1")
    suspend fun grantedEntry(permission: String): AgentPermissionEntity?

    @Query("SELECT permission FROM agent_permissions WHERE granted = 1")
    suspend fun allGrantedPermissions(): List<String>
}
