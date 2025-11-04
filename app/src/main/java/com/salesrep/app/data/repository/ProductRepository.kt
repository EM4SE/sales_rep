package com.salesrep.app.data.repository

import com.salesrep.app.data.local.dao.ProductDao
import com.salesrep.app.data.local.dao.SyncQueueDao
import com.salesrep.app.data.local.entities.SyncQueueEntity
import com.salesrep.app.data.mapper.toDomain
import com.salesrep.app.data.mapper.toEntity
import com.salesrep.app.data.remote.ApiService
import com.salesrep.app.data.remote.dto.CreateProductRequest
import com.salesrep.app.data.remote.dto.UpdateProductRequest
import com.salesrep.app.domain.model.Product
import com.salesrep.app.util.NetworkUtils
import com.salesrep.app.util.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val apiService: ApiService,
    private val productDao: ProductDao,
    private val syncQueueDao: SyncQueueDao,
    private val networkUtils: NetworkUtils,
    private val gson: Gson
) {
    fun getProducts(): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading())

        val localProducts = productDao.getAllProducts().first()
        emit(Resource.Success(localProducts.map { it.toDomain() }))

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.getProducts()
                if (response.isSuccessful) {
                    response.body()?.let { products ->
                        productDao.insertProducts(products.map { it.toEntity() })
                        val updatedProducts = productDao.getAllProducts().first()
                        emit(Resource.Success(updatedProducts.map { it.toDomain() }))
                    }
                }
            } catch (e: Exception) {
                // Continue with local data
            }
        }
    }

    fun getProductById(id: Int): Flow<Resource<Product>> = flow {
        emit(Resource.Loading())

        val localProduct = productDao.getProductById(id).first()
        localProduct?.let {
            emit(Resource.Success(it.toDomain()))
        }

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.getProductById(id)
                if (response.isSuccessful) {
                    response.body()?.product?.let { productDto ->
                        productDao.insertProduct(productDto.toEntity())
                        emit(Resource.Success(productDto.toEntity().toDomain()))
                    }
                }
            } catch (e: Exception) {
                if (localProduct == null) {
                    emit(Resource.Error(e.message ?: "Error loading product"))
                }
            }
        }
    }

    fun getProductsByCategory(categoryId: Int): Flow<Resource<List<Product>>> = flow {
        emit(Resource.Loading())
        val products = productDao.getProductsByCategory(categoryId).first()
        emit(Resource.Success(products.map { it.toDomain() }))
    }

    suspend fun createProduct(
        name: String,
        description: String?,
        price: Double,
        stock: Int,
        sku: String?,
        upc: String?,
        imageUrl: String?,
        categoryId: Int
    ): Flow<Resource<Product>> = flow {
        emit(Resource.Loading())

        val request = CreateProductRequest(name, description, price, stock, sku, upc, imageUrl, categoryId)

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.createProduct(request)
                if (response.isSuccessful) {
                    response.body()?.product?.let { productDto ->
                        productDao.insertProduct(productDto.toEntity())
                        emit(Resource.Success(productDto.toEntity().toDomain()))
                    }
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to create product"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            val syncItem = SyncQueueEntity(
                entityType = "product",
                entityId = "temp_${System.currentTimeMillis()}",
                operation = "CREATE",
                jsonData = gson.toJson(request),
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Product will be created when online"))
        }
    }

    suspend fun updateProduct(
        id: Int,
        name: String?,
        description: String?,
        price: Double?,
        stock: Int?,
        sku: String?,
        upc: String?,
        imageUrl: String?,
        categoryId: Int?
    ): Flow<Resource<Product>> = flow {
        emit(Resource.Loading())

        val request = UpdateProductRequest(name, description, price, stock, sku, upc, imageUrl, categoryId)

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.updateProduct(id, request)
                if (response.isSuccessful) {
                    response.body()?.product?.let { productDto ->
                        productDao.insertProduct(productDto.toEntity())
                        emit(Resource.Success(productDto.toEntity().toDomain()))
                    }
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to update product"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            val syncItem = SyncQueueEntity(
                entityType = "product",
                entityId = id.toString(),
                operation = "UPDATE",
                jsonData = gson.toJson(request),
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Product will be updated when online"))
        }
    }
}