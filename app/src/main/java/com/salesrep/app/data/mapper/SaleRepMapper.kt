package com.salesrep.app.data.mapper

import com.salesrep.app.data.local.entities.SaleRepEntity
import com.salesrep.app.data.remote.dto.SaleRepDto
import com.salesrep.app.domain.model.SaleRep

fun SaleRepDto.toEntity() = SaleRepEntity(
    id = id,
    userId = userId,
    name = name,
    email = email,
    phone = phone,
    region = region,
    profilePicture = profilePicture,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = 1
)

fun SaleRepEntity.toDomain() = SaleRep(
    id = id,
    userId = userId,
    name = name,
    email = email,
    phone = phone,
    region = region,
    profilePicture = profilePicture,
    isActive = isActive == 1,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun SaleRep.toEntity() = SaleRepEntity(
    id = id,
    userId = userId,
    name = name,
    email = email,
    phone = phone,
    region = region,
    profilePicture = profilePicture,
    isActive = if (isActive) 1 else 0,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = 1
)

fun SaleRepDto.toDomain(): SaleRep {
    return SaleRep(
        id = id,
        userId = userId,
        name = name,
        email = email,
        phone = phone,
        region = region,
        profilePicture = profilePicture,
        isActive = isActive == 1, // Convert Int (1/0) to Boolean
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}