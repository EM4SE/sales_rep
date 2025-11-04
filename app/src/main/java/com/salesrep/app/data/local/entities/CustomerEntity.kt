package com.salesrep.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "customers")
data class CustomerEntity(
    @PrimaryKey val id: Int,
    val saleRepId: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val address: String?,
    val city: String?,
    val latitude: Double?,
    val longitude: Double?,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Int = 1,
    val localId: String? = null
)