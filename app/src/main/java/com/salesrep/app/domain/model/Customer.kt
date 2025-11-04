package com.salesrep.app.domain.model

data class Customer(
    val id: Int,
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
    val saleRepName: String? = null
)