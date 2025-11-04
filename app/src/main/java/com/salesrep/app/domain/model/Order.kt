package com.salesrep.app.domain.model

data class Order(
    val id: Int,
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
    val customerName: String? = null,
    val saleRepName: String? = null,
    val items: List<OrderItem> = emptyList()
)

data class OrderItem(
    val id: Int,
    val orderId: Int,
    val productId: Int,
    val quantity: Int,
    val unitPrice: Double,
    val discount: Double?,
    val taxAmount: Double?,
    val productName: String? = null,
    val productImage: String? = null
)
