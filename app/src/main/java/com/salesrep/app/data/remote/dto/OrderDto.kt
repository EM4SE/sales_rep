package com.salesrep.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OrderDto(
    val id: Int,
    @SerializedName("customer_id")
    val customerId: Int,
    @SerializedName("sale_rep_id")
    val saleRepId: Int,
    @SerializedName("total_amount")
    val totalAmount: Double,
    val discount: Double?,
    val tax: Double?,
    val status: String,
    @SerializedName("signature_image")
    val signatureImage: String?,
    @SerializedName("delivered_at")
    val deliveredAt: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val customer: CustomerDto? = null,
    @SerializedName("saleRep")
    val saleRep: SaleRepDto? = null,
    @SerializedName("orderItems")
    val orderItems: List<OrderItemDto>? = null
)

data class OrderItemDto(
    val id: Int,
    @SerializedName("order_id")
    val orderId: Int,
    @SerializedName("product_id")
    val productId: Int,
    val quantity: Int,
    @SerializedName("unit_price")
    val unitPrice: Double,
    val discount: Double?,
    @SerializedName("tax_amount")
    val taxAmount: Double?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val product: ProductDto? = null
)

data class CreateOrderRequest(
    @SerializedName("customer_id")
    val customerId: Int,
    @SerializedName("total_amount")
    val totalAmount: Double,
    val discount: Double?,
    val tax: Double?,
    val status: String,
    @SerializedName("signature_image")
    val signatureImage: String?,
    @SerializedName("delivered_at")
    val deliveredAt: String?,
    val items: List<CreateOrderItemRequest>
)

data class CreateOrderItemRequest(
    @SerializedName("product_id")
    val productId: Int,
    val quantity: Int,
    @SerializedName("unit_price")
    val unitPrice: Double,
    val discount: Double?,
    @SerializedName("tax_amount")
    val taxAmount: Double?
)

data class UpdateOrderRequest(
    @SerializedName("customer_id")
    val customerId: Int?,
    @SerializedName("total_amount")
    val totalAmount: Double?,
    val discount: Double?,
    val tax: Double?,
    val status: String?,
    @SerializedName("signature_image")
    val signatureImage: String?,
    @SerializedName("delivered_at")
    val deliveredAt: String?
)

data class OrderResponse(
    val message: String,
    val order: OrderDto
)

data class OrdersListResponse(
    val data: List<OrderDto>
)