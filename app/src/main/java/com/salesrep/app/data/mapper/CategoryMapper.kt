package com.salesrep.app.data.mapper

import com.salesrep.app.data.local.entities.CategoryEntity
import com.salesrep.app.data.remote.dto.CategoryDto
import com.salesrep.app.domain.model.Category

fun CategoryDto.toEntity() = CategoryEntity(
    id = id,
    userId = userId,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = 1
)

fun CategoryEntity.toDomain() = Category(
    id = id,
    userId = userId,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Category.toEntity() = CategoryEntity(
    id = id,
    userId = userId,
    name = name,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = 1
)