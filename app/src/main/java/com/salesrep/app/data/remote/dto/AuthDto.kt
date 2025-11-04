package com.salesrep.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Login Request
data class LoginRequest(
    val email: String,
    val password: String
)

// Register Request
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("password_confirmation")
    val passwordConfirmation: String
)

// Auth Response (for both Admin and Sales Rep)
data class AuthResponse(
    val user: UserDto? = null,
    @SerializedName("saleRep")
    val saleRep: SaleRepDto? = null,
    val token: String,
    val message: String? = null
)

// User DTO
data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

// Logout Response
data class LogoutResponse(
    val message: String
)