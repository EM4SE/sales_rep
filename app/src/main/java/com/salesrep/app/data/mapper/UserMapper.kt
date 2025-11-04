package com.salesrep.app.data.mapper

import com.salesrep.app.data.local.entities.UserEntity
import com.salesrep.app.data.remote.dto.UserDto
import com.salesrep.app.domain.model.User

fun UserDto.toEntity() = UserEntity(
    id = id,
    name = name,
    email = email,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserEntity.toDomain() = User(
    id = id,
    name = name,
    email = email,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun UserDto.toDomain() = User(
    id = id,
    name = name,
    email = email,
    createdAt = createdAt,
    updatedAt = updatedAt
)