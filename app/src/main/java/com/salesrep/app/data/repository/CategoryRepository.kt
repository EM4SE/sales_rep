package com.salesrep.app.data.repository

import com.salesrep.app.data.local.dao.CategoryDao
import com.salesrep.app.data.local.dao.SyncQueueDao
import com.salesrep.app.data.local.entities.SyncQueueEntity
import com.salesrep.app.data.mapper.toDomain
import com.salesrep.app.data.mapper.toEntity
import com.salesrep.app.data.remote.ApiService
import com.salesrep.app.data.remote.dto.CreateCategoryRequest
import com.salesrep.app.data.remote.dto.UpdateCategoryRequest
import com.salesrep.app.domain.model.Category
import com.salesrep.app.util.NetworkUtils
import com.salesrep.app.util.Resource
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val apiService: ApiService,
    private val categoryDao: CategoryDao,
    private val syncQueueDao: SyncQueueDao,
    private val networkUtils: NetworkUtils,
    private val gson: Gson
) {
    fun getCategories(): Flow<Resource<List<Category>>> = flow {
        emit(Resource.Loading())

        // Emit local data first
        val localCategories = categoryDao.getAllCategories().first()
        emit(Resource.Success(localCategories.map { it.toDomain() }))

        // Try to fetch from network if available
        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.getCategories()
                if (response.isSuccessful) {
                    response.body()?.let { categories ->
                        categoryDao.insertCategories(categories.map { it.toEntity() })
                        val updatedCategories = categoryDao.getAllCategories().first()
                        emit(Resource.Success(updatedCategories.map { it.toDomain() }))
                    }
                }
            } catch (e: Exception) {
                // Continue with local data
            }
        }
    }

    fun getCategoryById(id: Int): Flow<Resource<Category>> = flow {
        emit(Resource.Loading())

        val localCategory = categoryDao.getCategoryById(id).first()
        localCategory?.let {
            emit(Resource.Success(it.toDomain()))
        }

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.getCategoryById(id)
                if (response.isSuccessful) {
                    response.body()?.category?.let { categoryDto ->
                        categoryDao.insertCategory(categoryDto.toEntity())
                        emit(Resource.Success(categoryDto.toEntity().toDomain()))
                    }
                }
            } catch (e: Exception) {
                if (localCategory == null) {
                    emit(Resource.Error(e.message ?: "Error loading category"))
                }
            }
        }
    }

    suspend fun createCategory(
        name: String,
        description: String?
    ): Flow<Resource<Category>> = flow {
        emit(Resource.Loading())

        val request = CreateCategoryRequest(name, description)

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.createCategory(request)
                if (response.isSuccessful) {
                    response.body()?.category?.let { categoryDto ->
                        categoryDao.insertCategory(categoryDto.toEntity())
                        emit(Resource.Success(categoryDto.toEntity().toDomain()))
                    }
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to create category"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            val syncItem = SyncQueueEntity(
                entityType = "category",
                entityId = "temp_${System.currentTimeMillis()}",
                operation = "CREATE",
                jsonData = gson.toJson(request),
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Category will be created when online"))
        }
    }

    suspend fun updateCategory(
        id: Int,
        name: String?,
        description: String?
    ): Flow<Resource<Category>> = flow {
        emit(Resource.Loading())

        val request = UpdateCategoryRequest(name, description)

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.updateCategory(id, request)
                if (response.isSuccessful) {
                    response.body()?.category?.let { categoryDto ->
                        categoryDao.insertCategory(categoryDto.toEntity())
                        emit(Resource.Success(categoryDto.toEntity().toDomain()))
                    }
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to update category"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            val syncItem = SyncQueueEntity(
                entityType = "category",
                entityId = id.toString(),
                operation = "UPDATE",
                jsonData = gson.toJson(request),
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Category will be updated when online"))
        }
    }

    suspend fun deleteCategory(id: Int): Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.deleteCategory(id)
                if (response.isSuccessful) {
                    categoryDao.deleteCategory(id)
                    emit(Resource.Success(Unit))
                } else {
                    emit(Resource.Error(response.message() ?: "Failed to delete category"))
                }
            } catch (e: Exception) {
                emit(Resource.Error(e.message ?: "An error occurred"))
            }
        } else {
            val syncItem = SyncQueueEntity(
                entityType = "category",
                entityId = id.toString(),
                operation = "DELETE",
                jsonData = "",
                timestamp = System.currentTimeMillis()
            )
            syncQueueDao.insertSyncItem(syncItem)
            emit(Resource.Error("Offline: Category will be deleted when online"))
        }
    }
}