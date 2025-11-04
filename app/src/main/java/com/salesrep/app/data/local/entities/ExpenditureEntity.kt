package com.salesrep.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenditures")
data class ExpenditureEntity(
    @PrimaryKey val id: Int,
    val saleRepId: Int,
    val title: String,
    val description: String?,
    val amount: Double,
    val date: String,
    val receiptImage: String?,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Int = 1,
    val localId: String? = null
)
