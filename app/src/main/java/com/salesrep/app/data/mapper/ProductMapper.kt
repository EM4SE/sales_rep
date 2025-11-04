package com.salesrep.app.data.mapper

import com.salesrep.app.data.local.entities.ProductEntity
import com.salesrep.app.data.remote.dto.ProductDto
import com.salesrep.app.domain.model.Product

fun ProductDto.toEntity() = ProductEntity(
    id = id,
    userId = userId,
    categoryId = categoryId,
    name = name,
    description = description,
    price = price,
    stock = stock,
    sku = sku,
    upc = upc,
    imageUrl = imageUrl,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = 1
)

fun ProductEntity.toDomain() = Product(
    id = id,
    userId = userId,
    categoryId = categoryId,
    name = name,
    description = description,
    price = price,
    stock = stock,
    sku = sku,
    upc = upc,
    imageUrl = imageUrl,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Product.toEntity() = ProductEntity(
    id = id,
    userId = userId,
    categoryId = categoryId,
    name = name,
    description = description,
    price = price,
    stock = stock,
    sku = sku,
    upc = upc,
    imageUrl = imageUrl,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = 1
)