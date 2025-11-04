package com.salesrep.app.domain.model

data class SaleRep(
    val id: Int,
    val userId: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val region: String?,
    val profilePicture: String?,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String
)
