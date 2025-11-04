package com.salesrep.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_items")
data class OrderItemEntity(
    @PrimaryKey val id: Int,
    val orderId: Int,
    val productId: Int,
    val quantity: Int,
    val unitPrice: Double,
    val discount: Double?,
    val taxAmount: Double?,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Int = 1,
    val localId: String? = null
)