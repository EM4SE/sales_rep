package com.salesrep.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sale_reps")
data class SaleRepEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val region: String?,
    val profilePicture: String?,
    val isActive: Int, // 1 = active, 0 = inactive
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Int = 1, // 1 = synced, 0 = pending sync
    val localId: String? = null // For offline created records
)