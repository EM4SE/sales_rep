package com.salesrep.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CustomerDto(
    val id: Int,
    @SerializedName("sale_rep_id")
    val saleRepId: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val address: String?,
    val city: String?,
    val latitude: Double?,
    val longitude: Double?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("sale_rep")
    val saleRep: SaleRepDto? = null
)

data class CreateCustomerRequest(
    val name: String,
    val email: String,
    val phone: String?,
    val address: String?,
    val city: String?,
    val latitude: Double?,
    val longitude: Double?
)

data class UpdateCustomerRequest(
    val name: String?,
    val email: String?,
    val phone: String?,
    val address: String?,
    val city: String?,
    val latitude: Double?,
    val longitude: Double?
)

data class CustomerResponse(
    val message: String,
    val customer: CustomerDto
)

data class CustomersListResponse(
    val data: List<CustomerDto>
)