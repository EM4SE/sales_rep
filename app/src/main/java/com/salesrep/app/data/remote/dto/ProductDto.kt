package com.salesrep.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProductDto(
    val id: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("category_id")
    val categoryId: Int,
    val name: String,
    val description: String?,
    val price: Double,
    val stock: Int,
    val sku: String?,
    val upc: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    val category: CategoryDto? = null
)

data class CreateProductRequest(
    val name: String,
    val description: String?,
    val price: Double,
    val stock: Int,
    val sku: String?,
    val upc: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("category_id")
    val categoryId: Int
)

data class UpdateProductRequest(
    val name: String?,
    val description: String?,
    val price: Double?,
    val stock: Int?,
    val sku: String?,
    val upc: String?,
    @SerializedName("image_url")
    val imageUrl: String?,
    @SerializedName("category_id")
    val categoryId: Int?
)

data class ProductResponse(
    val message: String,
    val product: ProductDto
)

data class ProductsListResponse(
    val data: List<ProductDto>
)