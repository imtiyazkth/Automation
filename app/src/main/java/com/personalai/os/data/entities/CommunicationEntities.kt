package com.personalai.os.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String?,
    val platformHandles: String? = null // JSON-encoded map of platform -> handle
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val contactId: String,
    val platform: String,        // whatsapp | telegram | sms | email
    val direction: String,       // inbound | outbound
    val body: String,
    val category: String?,       // AI-assigned category
    val automationStatus: String, // draft | sent | ignored | pending_approval
    val timestamp: Long
)
