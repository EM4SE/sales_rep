package com.salesrep.app.data.remote.dto

data class ErrorResponse(
    val message: String,
    val errors: Map<String, List<String>>? = null
)