package com.salesrep.app.data.mapper

import com.salesrep.app.data.local.entities.CustomerEntity
import com.salesrep.app.data.remote.dto.CustomerDto
import com.salesrep.app.domain.model.Customer

fun CustomerDto.toEntity() = CustomerEntity(
    id = id,
    saleRepId = saleRepId,
    name = name,
    email = email,
    phone = phone,
    address = address,
    city = city,
    latitude = latitude,
    longitude = longitude,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = 1
)

fun CustomerEntity.toDomain() = Customer(
    id = id,
    saleRepId = saleRepId,
    name = name,
    email = email,
    phone = phone,
    address = address,
    city = city,
    latitude = latitude,
    longitude = longitude,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Customer.toEntity() = CustomerEntity(
    id = id,
    saleRepId = saleRepId,
    name = name,
    email = email,
    phone = phone,
    address = address,
    city = city,
    latitude = latitude,
    longitude = longitude,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = 1
)