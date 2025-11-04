package com.salesrep.app.domain.model

data class Product(
    val id: Int,
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
    val categoryName: String? = null
)