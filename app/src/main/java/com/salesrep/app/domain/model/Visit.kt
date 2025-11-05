package com.salesrep.app.domain.model

data class Visit(
    val id: Int,
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
    // Customer details
    val customerName: String? = null,
    val customerAddress: String? = null,
    val customerPhone: String? = null,
    val customerEmail: String? = null,
    val customerCity: String? = null,
    // Sales Rep details
    val saleRepName: String? = null,
    val saleRepEmail: String? = null,
    val saleRepPhone: String? = null
)