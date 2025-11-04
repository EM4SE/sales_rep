package com.salesrep.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "visits")
data class VisitEntity(
    @PrimaryKey val id: Int,
    val saleRepId: Int,
    val customerId: Int,
    val visitDate: String,
    val visitTime: String,
    val visitType: String,
    val notes: String?,
    val locationLat: Double?,
    val locationLng: Double?,
    val status: String,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Int = 1,
    val localId: String? = null
)