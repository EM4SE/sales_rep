package com.salesrep.app.data.remote

import com.salesrep.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ========================================
    // AUTHENTICATION ENDPOINTS
    // ========================================

    @POST("register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("logout")
    suspend fun logout(): Response<LogoutResponse>

    // ========================================
    // SALES REP AUTHENTICATION
    // ========================================

    @POST("sale-reps/login")
    suspend fun saleRepLogin(@Body request: LoginRequest): Response<AuthResponse>

    @POST("sale-reps/logout")
    suspend fun saleRepLogout(): Response<LogoutResponse>

    // ========================================
    // SALES REPRESENTATIVES ENDPOINTS
    // ========================================

    @GET("sale-reps")
    suspend fun getSaleReps(): Response<List<SaleRepDto>>

    @GET("sale-reps/{id}")
    suspend fun getSaleRepById(@Path("id") id: Int): Response<SaleRepResponse>

    @POST("sale-reps")
    suspend fun createSaleRep(@Body request: CreateSaleRepRequest): Response<SaleRepResponse>

    @PUT("sale-reps/{id}")
    suspend fun updateSaleRep(
        @Path("id") id: Int,
        @Body request: UpdateSaleRepRequest
    ): Response<SaleRepResponse>

    @DELETE("sale-reps/{id}")
    suspend fun deleteSaleRep(@Path("id") id: Int): Response<LogoutResponse>

    // ========================================
    // CUSTOMERS ENDPOINTS
    // ========================================

    @GET("customers")
    suspend fun getCustomers(): Response<List<CustomerDto>>

    @GET("customers/{id}")
    suspend fun getCustomerById(@Path("id") id: Int): Response<CustomerResponse>

    @POST("customers")
    suspend fun createCustomer(@Body request: CreateCustomerRequest): Response<CustomerResponse>

    @PUT("customers/{id}")
    suspend fun updateCustomer(
        @Path("id") id: Int,
        @Body request: UpdateCustomerRequest
    ): Response<CustomerResponse>

    @DELETE("customers/{id}")
    suspend fun deleteCustomer(@Path("id") id: Int): Response<LogoutResponse>

    // ========================================
    // CATEGORIES ENDPOINTS
    // ========================================

    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>

    @GET("categories/{id}")
    suspend fun getCategoryById(@Path("id") id: Int): Response<CategoryResponse>

    @POST("categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): Response<CategoryResponse>

    @PUT("categories/{id}")
    suspend fun updateCategory(
        @Path("id") id: Int,
        @Body request: UpdateCategoryRequest
    ): Response<CategoryResponse>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Int): Response<LogoutResponse>

    // ========================================
    // PRODUCTS ENDPOINTS
    // ========================================

    @GET("products")
    suspend fun getProducts(): Response<List<ProductDto>>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Int): Response<ProductResponse>

    @POST("products")
    suspend fun createProduct(@Body request: CreateProductRequest): Response<ProductResponse>

    @PUT("products/{id}")
    suspend fun updateProduct(
        @Path("id") id: Int,
        @Body request: UpdateProductRequest
    ): Response<ProductResponse>

    @DELETE("products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<LogoutResponse>

    // ========================================
    // ORDERS ENDPOINTS
    // ========================================

    @GET("orders")
    suspend fun getOrders(): Response<List<OrderDto>>

    @GET("orders/{id}")
    suspend fun getOrderById(@Path("id") id: Int): Response<OrderResponse>

    @POST("orders")
    suspend fun createOrder(@Body request: CreateOrderRequest): Response<OrderResponse>

    @PUT("orders/{id}")
    suspend fun updateOrder(
        @Path("id") id: Int,
        @Body request: UpdateOrderRequest
    ): Response<OrderResponse>

    @DELETE("orders/{id}")
    suspend fun deleteOrder(@Path("id") id: Int): Response<LogoutResponse>

    // ========================================
    // VISITS ENDPOINTS
    // ========================================

    @GET("visits")
    suspend fun getVisits(): Response<List<VisitDto>>

    @GET("visits/{id}")
    suspend fun getVisitById(@Path("id") id: Int): Response<VisitResponse>

    @POST("visits")
    suspend fun createVisit(@Body request: CreateVisitRequest): Response<VisitResponse>

    @PUT("visits/{id}")
    suspend fun updateVisit(
        @Path("id") id: Int,
        @Body request: UpdateVisitRequest
    ): Response<VisitResponse>

    @DELETE("visits/{id}")
    suspend fun deleteVisit(@Path("id") id: Int): Response<LogoutResponse>

    // ========================================
    // EXPENDITURES ENDPOINTS
    // ========================================

    @GET("expenditures")
    suspend fun getExpenditures(): Response<List<ExpenditureDto>>

    @GET("expenditures/{id}")
    suspend fun getExpenditureById(@Path("id") id: Int): Response<ExpenditureResponse>

    @POST("expenditures")
    suspend fun createExpenditure(@Body request: CreateExpenditureRequest): Response<ExpenditureResponse>

    @PUT("expenditures/{id}")
    suspend fun updateExpenditure(
        @Path("id") id: Int,
        @Body request: UpdateExpenditureRequest
    ): Response<ExpenditureResponse>

    @DELETE("expenditures/{id}")
    suspend fun deleteExpenditure(@Path("id") id: Int): Response<LogoutResponse>
}