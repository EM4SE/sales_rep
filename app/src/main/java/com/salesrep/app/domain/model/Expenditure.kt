package com.salesrep.app.domain.model

data class Expenditure(
    val id: Int,
    val saleRepId: Int,
    val title: String,
    val description: String?,
    val amount: Double,
    val date: String,
    val receiptImage: String?,
    val createdAt: String,
    val updatedAt: String,
    val saleRepName: String? = null
)