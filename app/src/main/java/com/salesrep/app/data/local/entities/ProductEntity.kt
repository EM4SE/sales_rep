package com.salesrep.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val categoryId: Int,
    val name: String,
    val description: String?,
    val price: Double,
    val stock: Int,
    val sku: String?,
    val upc: String?,
    val imageUrl: String?,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Int = 1,
    val localId: String? = null
)