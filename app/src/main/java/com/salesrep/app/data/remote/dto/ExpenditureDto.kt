package com.salesrep.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ExpenditureDto(
    val id: Int,
    @SerializedName("sale_rep_id")
    val saleRepId: Int,
    val title: String,
    val description: String?,
    val amount: Double,
    val date: String,
    @SerializedName("receipt_image")
    val receiptImage: String?,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("saleRep")
    val saleRep: SaleRepDto? = null
)

data class CreateExpenditureRequest(
    val title: String,
    val description: String?,
    val amount: Double,
    val date: String,
    @SerializedName("receipt_image")
    val receiptImage: String?
)

data class UpdateExpenditureRequest(
    val title: String?,
    val description: String?,
    val amount: Double?,
    val date: String?,
    @SerializedName("receipt_image")
    val receiptImage: String?
)

data class ExpenditureResponse(
    val message: String,
    val expenditure: ExpenditureDto
)

data class ExpendituresListResponse(
    val data: List<ExpenditureDto>
)