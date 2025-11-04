package com.salesrep.app.data.mapper

import com.salesrep.app.data.local.entities.ExpenditureEntity
import com.salesrep.app.data.remote.dto.ExpenditureDto
import com.salesrep.app.domain.model.Expenditure

fun ExpenditureDto.toEntity() = ExpenditureEntity(
    id = id,
    saleRepId = saleRepId,
    title = title,
    description = description,
    amount = amount,
    date = date,
    receiptImage = receiptImage,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = 1
)

fun ExpenditureEntity.toDomain() = Expenditure(
    id = id,
    saleRepId = saleRepId,
    title = title,
    description = description,
    amount = amount,
    date = date,
    receiptImage = receiptImage,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Expenditure.toEntity() = ExpenditureEntity(
    id = id,
    saleRepId = saleRepId,
    title = title,
    description = description,
    amount = amount,
    date = date,
    receiptImage = receiptImage,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = 1
)