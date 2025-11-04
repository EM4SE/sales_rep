package com.salesrep.app.presentation.salereps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.salesrep.app.data.local.dao.SaleRepDao
import com.salesrep.app.data.local.dao.SyncQueueDao
import com.salesrep.app.data.local.entities.SyncQueueEntity
import com.salesrep.app.data.mapper.toDomain
import com.salesrep.app.data.mapper.toEntity
import com.salesrep.app.data.remote.ApiService
import com.salesrep.app.data.remote.dto.CreateSaleRepRequest
import com.salesrep.app.data.remote.dto.UpdateSaleRepRequest
import com.salesrep.app.domain.model.SaleRep
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

data class SaleRepUiState(
    val isLoading: Boolean = false,
    val saleReps: List<SaleRep> = emptyList(),
    val selectedSaleRep: SaleRep? = null,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class SaleRepViewModel @Inject constructor(
    private val apiService: ApiService,
    private val saleRepDao: SaleRepDao,
    private val syncQueueDao: SyncQueueDao,
    private val networkUtils: NetworkUtils,
    private val gson: Gson
) : ViewModel() {

    private val _uiState = MutableStateFlow(SaleRepUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSaleReps()
    }

    fun loadSaleReps() {
        viewModelScope.launch {
            getSaleReps().collect { result ->
                when (result) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                saleReps = result.data ?: emptyList(),
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

    fun loadSaleRepById(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val localSaleRep = saleRepDao.getSaleRepById(id).first()
            localSaleRep?.let {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        selectedSaleRep = localSaleRep.toDomain()
                    )
                }
            }

            if (networkUtils.isNetworkAvailable()) {
                try {
                    val response = apiService.getSaleRepById(id)
                    if (response.isSuccessful) {
                        response.body()?.saleRep?.let { saleRepDto ->
                            saleRepDao.insertSaleRep(saleRepDto.toEntity())
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    selectedSaleRep = saleRepDto.toEntity().toDomain()
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (localSaleRep == null) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = e.message ?: "Error loading sale rep"
                            )
                        }
                    }
                }
            }
        }
    }

    private fun getSaleReps() = flow {
        emit(Resource.Loading())

        val localSaleReps = saleRepDao.getAllSaleReps().first()
        emit(Resource.Success(localSaleReps.map { it.toDomain() }))

        if (networkUtils.isNetworkAvailable()) {
            try {
                val response = apiService.getSaleReps()
                if (response.isSuccessful) {
                    response.body()?.let { saleReps ->
                        saleRepDao.insertSaleReps(saleReps.map { it.toEntity() })
                        val updatedSaleReps = saleRepDao.getAllSaleReps().first()
                        emit(Resource.Success(updatedSaleReps.map { it.toDomain() }))
                    }
                }
            } catch (e: Exception) {
                // Continue with local data
            }
        }
    }

    fun createSaleRep(
        name: String,
        email: String,
        password: String,
        passwordConfirmation: String,
        phone: String?,
        region: String?,
        profilePicture: String?,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val request = CreateSaleRepRequest(
                name = name,
                email = email,
                password = password,
                passwordConfirmation = passwordConfirmation,
                phone = phone,
                region = region,
                profilePicture = profilePicture,
                isActive = if (isActive) 1 else 0
            )

            if (networkUtils.isNetworkAvailable()) {
                try {
                    val response = apiService.createSaleRep(request)
                    if (response.isSuccessful) {
                        response.body()?.saleRep?.let { saleRepDto ->
                            saleRepDao.insertSaleRep(saleRepDto.toEntity())
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    successMessage = "Sales Rep created successfully"
                                )
                            }
                            loadSaleReps()
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = response.message() ?: "Failed to create sales rep"
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
                    entityType = "sale_rep",
                    entityId = "temp_${System.currentTimeMillis()}",
                    operation = "CREATE",
                    jsonData = gson.toJson(request),
                    timestamp = System.currentTimeMillis()
                )
                syncQueueDao.insertSyncItem(syncItem)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Offline: Sales Rep will be created when online"
                    )
                }
            }
        }
    }

    fun updateSaleRep(
        id: Int,
        name: String,
        email: String,
        password: String?,
        passwordConfirmation: String?,
        phone: String?,
        region: String?,
        profilePicture: String?,
        isActive: Boolean
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val request = UpdateSaleRepRequest(
                name = name,
                email = email,
                password = password,
                passwordConfirmation = passwordConfirmation,
                phone = phone,
                region = region,
                profilePicture = profilePicture,
                isActive = if (isActive) 1 else 0
            )

            if (networkUtils.isNetworkAvailable()) {
                try {
                    val response = apiService.updateSaleRep(id, request)
                    if (response.isSuccessful) {
                        response.body()?.saleRep?.let { saleRepDto ->
                            saleRepDao.insertSaleRep(saleRepDto.toEntity())
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    successMessage = "Sales Rep updated successfully"
                                )
                            }
                            loadSaleReps()
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = response.message() ?: "Failed to update sales rep"
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
                    entityType = "sale_rep",
                    entityId = id.toString(),
                    operation = "UPDATE",
                    jsonData = gson.toJson(request),
                    timestamp = System.currentTimeMillis()
                )
                syncQueueDao.insertSyncItem(syncItem)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Offline: Sales Rep will be updated when online"
                    )
                }
            }
        }
    }

    fun deleteSaleRep(id: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            if (networkUtils.isNetworkAvailable()) {
                try {
                    val response = apiService.deleteSaleRep(id)
                    if (response.isSuccessful) {
                        saleRepDao.deleteSaleRep(id)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                successMessage = "Sales Rep deleted successfully"
                            )
                        }
                        loadSaleReps()
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = response.message() ?: "Failed to delete sales rep"
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
                    entityType = "sale_rep",
                    entityId = id.toString(),
                    operation = "DELETE",
                    jsonData = "",
                    timestamp = System.currentTimeMillis()
                )
                syncQueueDao.insertSyncItem(syncItem)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Offline: Sales Rep will be deleted when online"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}