package com.salesrep.app.data.mapper

import com.salesrep.app.data.local.entities.VisitEntity
import com.salesrep.app.data.remote.dto.VisitDto
import com.salesrep.app.domain.model.Visit

fun VisitDto.toEntity() = VisitEntity(
    id = id,
    saleRepId = saleRepId,
    customerId = customerId,
    visitDate = visitDate,
    visitTime = visitTime,
    visitType = visitType,
    notes = notes,
    locationLat = locationLat,
    locationLng = locationLng,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = 1
)

fun VisitEntity.toDomain() = Visit(
    id = id,
    saleRepId = saleRepId,
    customerId = customerId,
    visitDate = visitDate,
    visitTime = visitTime,
    visitType = visitType,
    notes = notes,
    locationLat = locationLat,
    locationLng = locationLng,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Visit.toEntity() = VisitEntity(
    id = id,
    saleRepId = saleRepId,
    customerId = customerId,
    visitDate = visitDate,
    visitTime = visitTime,
    visitType = visitType,
    notes = notes,
    locationLat = locationLat,
    locationLng = locationLng,
    status = status,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = 1
)
