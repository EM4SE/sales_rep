package com.salesrep.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class SaleRepDto(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val region: String?,
    @SerializedName("profile_picture")
    val profilePicture: String?,
    @SerializedName("is_active")
    val isActive: Int, // API expects 1 or 0
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class CreateSaleRepRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirmation")
    val passwordConfirmation: String,
    val phone: String?,
    val region: String?,
    @SerializedName("profile_picture")
    val profilePicture: String?,
    @SerializedName("is_active")
    val isActive: Int = 1
)

data class UpdateSaleRepRequest(
    val name: String?,
    val email: String?,
    val password: String?,
    @SerializedName("password_confirmation")
    val passwordConfirmation: String?,
    val phone: String?,
    val region: String?,
    @SerializedName("profile_picture")
    val profilePicture: String?,
    @SerializedName("is_active")
    val isActive: Int?
)

data class SaleRepResponse(
    val message: String,
    @SerializedName("saleRep")
    val saleRep: SaleRepDto
)

data class SaleRepsListResponse(
    val data: List<SaleRepDto>
)