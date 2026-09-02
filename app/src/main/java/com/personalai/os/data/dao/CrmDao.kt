package com.personalai.os.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.personalai.os.data.entities.CrmLeadEntity
import com.personalai.os.data.entities.OrderEntity

@Dao
interface CrmDao {
    @Insert
    suspend fun insertLead(lead: CrmLeadEntity)

    @Update
    suspend fun updateLead(lead: CrmLeadEntity)

    @Query("SELECT * FROM crm_leads WHERE status != 'COMPLETED' ORDER BY createdAt DESC")
    suspend fun activeLeads(): List<CrmLeadEntity>

    @Insert
    suspend fun insertOrder(order: OrderEntity)

    @Query("SELECT * FROM orders WHERE createdAt >= :sinceEpochMillis")
    suspend fun ordersSince(sinceEpochMillis: Long): List<OrderEntity>
}
