package com.salesrep.app.presentation.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val apiService: ApiService,
    private val categoryDao: CategoryDao,
    private val syncQueueDao: SyncQueueDao,
    private val networkUtils: NetworkUtils,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            getCategories().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                categories = result.data ?: emptyList(),
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getCategories() = flow {
        emit(Resource.Loading())

        val localCategories = categoryDao.getAllCategories().first()
        emit(Resource.Success(localCategories.map { it.toDomain() }))

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

    fun createCategory(name: String, description: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val request = CreateCategoryRequest(name, description)

            if (networkUtils.isNetworkAvailable()) {
                try {
                    val response = apiService.createCategory(request)
                    if (response.isSuccessful) {
                        response.body()?.category?.let { categoryDto ->
                            categoryDao.insertCategory(categoryDto.toEntity())
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    successMessage = "Category created successfully"
                                )
                            }
                            loadCategories()
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = response.message() ?: "Failed to create category"
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "An error occurred"
                        )
                    }
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Offline: Category will be created when online"
                    )
                }
            }
        }
    }

    fun deleteCategory(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            if (networkUtils.isNetworkAvailable()) {
                try {
                    val response = apiService.deleteCategory(id)
                    if (response.isSuccessful) {
                        categoryDao.deleteCategory(id)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Category deleted successfully"
                            )
                        }
                        loadCategories()
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = response.message() ?: "Failed to delete category"
                            )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "An error occurred"
                        )
                    }
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
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Offline: Category will be deleted when online"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}