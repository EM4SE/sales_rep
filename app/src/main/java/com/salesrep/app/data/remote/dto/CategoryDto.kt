package com.salesrep.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CategoryDto(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    val name: String,
    val description: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String
)

data class CreateCategoryRequest(
    val name: String,
    val description: String?
)

data class UpdateCategoryRequest(
    val name: String?,
    val description: String?
)

data class CategoryResponse(
    val message: String,
    val category: CategoryDto
)

data class CategoriesListResponse(
    val data: List<CategoryDto>
)
