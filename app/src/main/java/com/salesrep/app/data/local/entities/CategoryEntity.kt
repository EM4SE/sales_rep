package com.salesrep.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: Int,
    val userId: Int,
    val name: String,
    val description: String?,
    val createdAt: String,
    val updatedAt: String,
    val isSynced: Int = 1,
    val localId: String? = null
)