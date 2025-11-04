package com.salesrep.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val id: Int,
    val customerId: Int,
    val saleRepId: Int,
    val totalAmount: Double,
    val discount: Double?,
    val tax: Double?,
    val status: String,
    val signatureImage: String?,
    val deliveredAt: String?,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Int = 1,
    val localId: String? = null
)