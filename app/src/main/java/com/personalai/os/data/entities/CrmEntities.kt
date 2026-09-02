package com.personalai.os.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crm_leads")
data class CrmLeadEntity(
    @PrimaryKey val id: String,
    val contactId: String,
    val source: String,
    val interest: String?,
    val status: String,   // NEW, CONTACTED, INTERESTED, ORDER_REQUESTED, ORDER_CONFIRMED, PAID, DELIVERED, COMPLETED
    val followUpDate: Long?,
    val notes: String?,
    val priority: String,
    val createdAt: Long
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: String,
    val leadId: String,
    val product: String,
    val status: String,
    val amount: Double?,
    val createdAt: Long
)
