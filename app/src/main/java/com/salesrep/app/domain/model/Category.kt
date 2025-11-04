package com.salesrep.app.domain.model

data class Category(
    val id: Int,
    val userId: Int,
    val name: String,
    val description: String?,
    val createdAt: String,
    val updatedAt: String
)