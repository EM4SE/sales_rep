package com.salesrep.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_queue")
data class SyncQueueEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val entityType: String, // "customer", "order", "visit", etc.
    val entityId: String, // Local or remote ID
    val operation: String, // "CREATE", "UPDATE", "DELETE"
    val jsonData: String, // Serialized entity data
    val timestamp: Long,
    val retryCount: Int = 0,
    val errorMessage: String? = null
)