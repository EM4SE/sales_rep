package com.salesrep.app.data.mapper

import com.salesrep.app.data.local.entities.OrderEntity
import com.salesrep.app.data.local.entities.OrderItemEntity
import com.salesrep.app.data.remote.dto.OrderDto
import com.salesrep.app.data.remote.dto.OrderItemDto
import com.salesrep.app.domain.model.Order
import com.salesrep.app.domain.model.OrderItem

fun OrderDto.toEntity() = OrderEntity(
    id = id,
    customerId = customerId,
    saleRepId = saleRepId,
    totalAmount = totalAmount,
    discount = discount,
    tax = tax,
    status = status,
    signatureImage = signatureImage,
    deliveredAt = deliveredAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = 1
)

fun OrderEntity.toDomain() = Order(
    id = id,
    customerId = customerId,
    saleRepId = saleRepId,
    totalAmount = totalAmount,
    discount = discount,
    tax = tax,
    status = status,
    signatureImage = signatureImage,
    deliveredAt = deliveredAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun OrderItemDto.toEntity() = OrderItemEntity(
    id = id,
    orderId = orderId,
    productId = productId,
    quantity = quantity,
    unitPrice = unitPrice,
    discount = discount,
    taxAmount = taxAmount,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = 1
)

fun OrderItemEntity.toDomain() = OrderItem(
    id = id,
    orderId = orderId,
    productId = productId,
    quantity = quantity,
    unitPrice = unitPrice,
    discount = discount,
    taxAmount = taxAmount
)