package com.salesrep.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class VisitDto(
    val id: Int,
    @SerializedName("sale_rep_id")
    val saleRepId: Int,
    @SerializedName("customer_id")
    val customerId: Int,
    @SerializedName("visit_date")
    val visitDate: String,
    @SerializedName("visit_time")
    val visitTime: String,
    @SerializedName("visit_type")
    val visitType: String,
    val notes: String?,
    @SerializedName("location_lat")
    val locationLat: Double?,
    @SerializedName("location_lng")
    val locationLng: Double?,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("salesRep")
    val salesRep: SaleRepDto? = null
)

data class CreateVisitRequest(
    @SerializedName("customer_id")
    val customerId: Int,
    @SerializedName("visit_date")
    val visitDate: String,
    @SerializedName("visit_time")
    val visitTime: String,
    @SerializedName("visit_type")
    val visitType: String,
    val notes: String?,
    @SerializedName("location_lat")
    val locationLat: Double?,
    @SerializedName("location_lng")
    val locationLng: Double?,
    val status: String
)

data class UpdateVisitRequest(
    @SerializedName("customer_id")
    val customerId: Int?,
    @SerializedName("visit_date")
    val visitDate: String?,
    @SerializedName("visit_time")
    val visitTime: String?,
    @SerializedName("visit_type")
    val visitType: String?,
    val notes: String?,
    @SerializedName("location_lat")
    val locationLat: Double?,
    @SerializedName("location_lng")
    val locationLng: Double?,
    val status: String?
)

data class VisitResponse(
    val message: String,
    val visit: VisitDto
)

data class VisitsListResponse(
    val data: List<VisitDto>
)