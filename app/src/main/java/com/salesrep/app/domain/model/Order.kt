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
    // Customer details
    val customerName: String? = null,
    val customerEmail: String? = null,
    val customerPhone: String? = null,
    val customerAddress: String? = null,
    val customerCity: String? = null,
    // Sales rep details
    val saleRepName: String? = null,
    val saleRepEmail: String? = null,
    val saleRepPhone: String? = null,
    val saleRepRegion: String? = null,
    // Order items
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
    val productImage: String? = null,
    val productDescription: String? = null
)